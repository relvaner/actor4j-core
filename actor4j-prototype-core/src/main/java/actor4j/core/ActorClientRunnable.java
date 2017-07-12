/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core;

import actor4j.core.messages.ActorMessage;

public interface ActorClientRunnable {
	public void runViaAlias(ActorMessage<?> message, String alias);
	public void runViaPath(ActorMessage<?> message, ActorServiceNode node, String path);
}
