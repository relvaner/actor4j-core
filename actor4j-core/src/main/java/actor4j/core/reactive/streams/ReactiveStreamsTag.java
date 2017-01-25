/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.reactive.streams;

public final class ReactiveStreamsTag {
	public static int SUBSCRIPTION_REQUEST       = 200;
	public static int SUBSCRIPTION_RESET_REQUEST = 201;
	public static int SUBSCRIPTION_CANCEL        = 202;
	public static int ON_NEXT                    = 203;
	public static int ON_ERROR                   = 204;
	public static int ON_COMPLETE                = 205;
}
