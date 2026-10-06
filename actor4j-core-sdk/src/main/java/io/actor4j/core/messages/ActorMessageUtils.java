/*
 * Copyright (c) 2015-2026, David A. Bauer. All rights reserved.
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
package io.actor4j.core.messages;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import io.actor4j.core.utils.Shareable;

public final class ActorMessageUtils {
	public static final Set<Class<?>> SUPPORTED_TYPES;

	public static boolean isSupportedType(Class<?> type) {
		return SUPPORTED_TYPES.contains(type)
			|| Enum.class.isAssignableFrom(type)
			|| Path.class.isAssignableFrom(type)
			|| ZoneId.class.isAssignableFrom(type);
	}
	
	public static <T> boolean equals(T o1, T o2) {	
		if (o1!=null/* && (o1 instanceof Comparable)*/) 
			return o1.equals(o2);
		else
			return o2==null;
	}

	static {
		SUPPORTED_TYPES = ConcurrentHashMap.newKeySet();
		Collections.addAll(SUPPORTED_TYPES,
			// java.lang
			Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
			Character.class, String.class, Boolean.class, Object.class,
			// java.math
			BigInteger.class, BigDecimal.class,
			// java.time
			Instant.class, Duration.class, Period.class,
			LocalDate.class, LocalTime.class, LocalDateTime.class,
			OffsetDateTime.class, OffsetTime.class, ZonedDateTime.class,
			Year.class, YearMonth.class, MonthDay.class, ZoneOffset.class,
			// java.util
			UUID.class, Locale.class, Currency.class,
			Optional.class, OptionalInt.class, OptionalLong.class, OptionalDouble.class,
			// java.util.regex, java.net
			Pattern.class, URI.class, URL.class
		);
	}
	
	public static boolean isShareable(Object value) {
		return value==null
			|| isSupportedType(value.getClass())
			|| value instanceof Record
			|| value instanceof Shareable;
	}
}
