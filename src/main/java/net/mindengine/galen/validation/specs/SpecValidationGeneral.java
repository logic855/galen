/*******************************************************************************
* Copyright 2013 Ivan Shubin http://mindengine.net
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
******************************************************************************/
package net.mindengine.galen.validation.specs;

import static java.lang.String.format;

import java.util.LinkedList;
import java.util.List;

import net.mindengine.galen.page.PageElement;
import net.mindengine.galen.page.Rect;
import net.mindengine.galen.specs.Location;
import net.mindengine.galen.specs.Range;
import net.mindengine.galen.specs.Side;
import net.mindengine.galen.specs.SpecComplex;
import net.mindengine.galen.validation.PageValidation;
import net.mindengine.galen.validation.SpecValidation;
import net.mindengine.galen.validation.ValidationError;

/**
 * Used for specs 'inside' and 'near'
 * @author ishubin
 *
 * @param <T>
 */
public abstract class SpecValidationGeneral<T extends SpecComplex> extends SpecValidation<T>{

    public SpecValidationGeneral(PageValidation pageValidation) {
        super(pageValidation);
    }

    @Override
    public ValidationError check(String objectName, T spec) {
        PageElement mainObject = getPageElement(objectName);
        
        ValidationError error = checkAvailability(mainObject, objectName);
        if (error != null) {
            return error;
        }
        
        PageElement secondObject = getPageElement(spec.getObject());
        error = checkAvailability(secondObject, spec.getObject());
        if (error != null) {
            return error;
        }
        
        Rect mainArea = mainObject.getArea();
        Rect secondArea = secondObject.getArea();
        
        List<String> messages = new LinkedList<String>();
        
        for (Location location : spec.getLocations()) {
            String message = verifyLocation(mainArea, secondArea, location);
            if (message != null) {
                messages.add(message);
            }
        }
        
        if (messages.size() > 0) {
            return new ValidationError(mainObject.getArea(), createMessage(messages, objectName)); 
        }
        else return null;
    }

    private String createMessage(List<String> messages, String objectName) {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append(format("\"%s\" ", objectName));
        boolean comma = false;
        for (String message : messages) {
            if (comma) {
                buffer.append(", ");
            }
            buffer.append("is ");
            buffer.append(message);
            comma = true;
        }
        return buffer.toString();
    }

    private String verifyLocation(Rect mainArea, Rect secondArea, Location location) {
        List<String> messages = new LinkedList<String>();
        Range range = location.getRange();
        
        for (Side side : location.getSides()) {
            int offset = getOffsetForSide(mainArea, secondArea, side);
            if (!range.holds(offset)) {
                messages.add(format("%dpx %s", offset, side));
            }
        }
        
        if (messages.size() > 0) {
            StringBuffer buffer = new StringBuffer();
            boolean comma = false;
            for (String message : messages) {
                if (comma) {
                    buffer.append(" and ");
                }
                buffer.append(message);
                comma = true;
            }
            if (range.isExact()) {
                buffer.append(format(" instead of %dpx", range.getFrom()));
            }
            else {
                buffer.append(format(" which is not in range of %dpx to %dpx", range.getFrom(), range.getTo()));
            }
            
            return buffer.toString(); 
        }
        else return null;
    }

    protected abstract int getOffsetForSide(Rect mainArea, Rect secondArea, Side side);
    
}