/*******************************************************************************
 * Copyright (c) 2021 Eclipse RDF4J contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *******************************************************************************/
package org.eclipse.rdf4j.sail.lmdb;

import java.io.File;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.lmdb.config.LmdbStoreConfig;
import org.eclipse.rdf4j.testsuite.repository.RepositoryTest;
import org.junit.jupiter.api.io.TempDir;

public class LmdbStoreRepositoryTest extends RepositoryTest {
	@TempDir
	public File dataDir;

	@Override
	protected Repository createRepository() {
		return new SailRepository(new LmdbStore(dataDir, new LmdbStoreConfig("spoc")));
	}
}
