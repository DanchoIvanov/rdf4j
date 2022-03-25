/*******************************************************************************
 * Copyright (c) 2015 Eclipse RDF4J contributors, Aduna, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.query.algebra.evaluation.iterator;

import java.util.NoSuchElementException;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.EmptyIteration;
import org.eclipse.rdf4j.common.iteration.LookAheadIteration;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.QueryEvaluationStep;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.QueryEvaluationContext;

/**
 * Interleaved join iterator.
 *
 * This join iterator produces results by interleaving results from its left argument into its right argument to speed
 * up bindings and produce fail-fast results. Note that this join strategy is only valid in cases where all bindings
 * from the left argument can be considered in scope for the right argument.
 *
 * @author Jeen Broekstra
 *
 */
public class JoinIterator extends LookAheadIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final CloseableIteration<? extends BindingSet, QueryEvaluationException> leftIter;

	private CloseableIteration<? extends BindingSet, QueryEvaluationException> rightIter;

	private final QueryEvaluationStep preparedRight;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public JoinIterator(QueryEvaluationStep leftPrepared, QueryEvaluationStep rightPrepared, BindingSet bindings)
			throws QueryEvaluationException {
		leftIter = leftPrepared.evaluate(bindings);

		// Initialize with empty iteration so that var is never null
		rightIter = QueryEvaluationStep.EMPTY_ITERATION;
		this.preparedRight = rightPrepared;
	}

	public JoinIterator(EvaluationStrategy strategy, Join join, BindingSet bindings, QueryEvaluationContext context)
			throws QueryEvaluationException {
		leftIter = strategy.evaluate(join.getLeftArg(), bindings);

		// Initialize with empty iteration so that var is never null
		rightIter = QueryEvaluationStep.EMPTY_ITERATION;
		preparedRight = strategy.precompile(join.getRightArg(), context);
		join.setAlgorithm(this);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected BindingSet getNextElement() throws QueryEvaluationException {
		try {
			while (leftIter.hasNext() || rightIter.hasNext()) {
				if (rightIter != QueryEvaluationStep.EMPTY_ITERATION) {
					if (rightIter.hasNext()) {
						return rightIter.next();
					}
					// Right iteration exhausted
					rightIter.close();
				}

				if (leftIter.hasNext()) {
					rightIter = preparedRight.evaluate(leftIter.next());
				}
			}
		} catch (NoSuchElementException ignore) {
			// probably, one of the iterations has been closed concurrently in
			// handleClose()
		}

		return null;
	}

	@Override
	protected void handleClose() throws QueryEvaluationException {
		try {
			leftIter.close();
		} finally {
			rightIter.close();
		}
	}
}
