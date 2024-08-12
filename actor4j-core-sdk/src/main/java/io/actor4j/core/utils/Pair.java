/*
 * Copyright (c) 2015-2024, David A. Bauer. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.actor4j.core.utils;

public record Pair<A, B>(A a, B b) {
	public static <A, B> Pair<A, B> of(A a, B b) {
		return new Pair<A, B>(a, b);
	}
	
	public A left() {
		return a;
	}
	
	public B right() {
		return b;
	}
	
	public A key() {
		return a;
	}
	
	public B value() {
		return b;
	}
	
	public B entity() {
		return b;
	}
}
