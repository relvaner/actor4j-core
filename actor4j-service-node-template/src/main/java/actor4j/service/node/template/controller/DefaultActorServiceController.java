/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.service.node.template.controller;

import actor4j.service.node.template.startup.DefaultActorService;

public class DefaultActorServiceController {
	public String isOnline() {
		return DefaultActorService.getService()!=null ? "is" : "is not";
	}
	
	public String getName() {
		return DefaultActorService.getService()!=null ? DefaultActorService.getService().getServiceNodeName() : "not available";
	}
}
