/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.testing;

import java.util.List;

import bdd4j.Story;

public interface ActorTest {
	public List<Story> test();
}
