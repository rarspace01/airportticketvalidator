/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rarspace01.airportticketvalidator.bcbp.model;

import org.rarspace01.airportticketvalidator.bcbp.specs.Element;
import org.rarspace01.airportticketvalidator.bcbp.specs.Occurrence;

import java.util.Map;

import static org.rarspace01.airportticketvalidator.bcbp.specs.Element.Type.COND;
import static org.rarspace01.airportticketvalidator.bcbp.specs.Element.Type.MAN;
import static org.rarspace01.airportticketvalidator.bcbp.specs.Element.Type.NONE;
import static org.rarspace01.airportticketvalidator.bcbp.specs.Element.Type.SEC;
import static org.rarspace01.airportticketvalidator.bcbp.specs.Occurrence.R;
import static org.rarspace01.airportticketvalidator.bcbp.specs.Occurrence.U;

public final class IataCodes {
    private IataCodes() { /* ... */ }

    public static <E extends Exception> void walk(IataCode iataCode, Visitor<E> visitor) throws E {
        visit(MAN, U, iataCode.getElements(), visitor);
        visit(MAN, R, iataCode.getFirstFlightSegment().getElements(), visitor);
        visit(COND, U, iataCode.getElements(), visitor);
        for (FlightSegment segment : iataCode.getFlightSegments()) {
            visit(MAN, R, segment.getElements(), visitor);
            visit(COND, R, segment.getElements(), visitor);
            visit(NONE, R, segment.getElements(), visitor);
        }
        visit(SEC, U, iataCode.getElements(), visitor);
    }

    private static <E extends Exception> void visit(Element.Type type, Occurrence occurrence,
                                                    Map<Element, CharSequence> elements, Visitor<E> visitor) throws E {
        for (Element e : Element.values()) {
            if (e.getType().equals(type) && e.getOccurrence().equals(occurrence)) {
                CharSequence value = elements.get(e);
                if (value != null) {
                    visitor.onElement(e, value);
                }
            }
        }
    }
}
