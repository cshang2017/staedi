/*******************************************************************************
 * Copyright 2017 xlate.io LLC, http://www.xlate.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package io.xlate.edi.internal.stream;

import io.xlate.edi.schema.EDISchemaException;
import io.xlate.edi.schema.Schema;
import io.xlate.edi.stream.EDIStreamEvent;
import io.xlate.edi.stream.EDIStreamException;
import io.xlate.edi.stream.EDIStreamFilter;
import io.xlate.edi.stream.EDIStreamReader;
import io.xlate.edi.stream.EDIStreamValidationError;
import io.xlate.edi.stream.Location;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

class StaEDIFilteredStreamReader implements EDIStreamReader {

    private final EDIStreamReader delegate;
    private final EDIStreamFilter filter;
    private EDIStreamEvent peekEvent = null;

    public StaEDIFilteredStreamReader(EDIStreamReader delegate, EDIStreamFilter filter) {
        this.delegate = delegate;
        this.filter = filter;
    }

    @Override
    public Object getProperty(String name) {
        return delegate.getProperty(name);
    }

    @Override
    public Map<String, Character> getDelimiters() {
        return delegate.getDelimiters();
    }

    @Override
    public EDIStreamEvent next() throws EDIStreamException {
        EDIStreamEvent event;

        if (peekEvent != null) {
            event = peekEvent;
            peekEvent = null;
            return event;
        }

        do {
            event = delegate.next();
        } while (!filter.accept(delegate));

        return event;
    }

    @Override
    public EDIStreamEvent nextTag() throws EDIStreamException {
        if (peekEvent == EDIStreamEvent.START_SEGMENT) {
            peekEvent = null;
            return EDIStreamEvent.START_SEGMENT;
        }

        EDIStreamEvent event;

        do {
            event = delegate.nextTag();
        } while (!filter.accept(delegate));

        return event;
    }

    @Override
    public boolean hasNext() throws EDIStreamException {
        while (delegate.hasNext()) {
            EDIStreamEvent event = delegate.next();

            if (filter.accept(delegate)) {
                peekEvent = event;
                return true;
            }
        }

        return false;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public EDIStreamEvent getEventType() {
        return delegate.getEventType();
    }

    @Override
    public String getStandard() {
        return delegate.getStandard();
    }

    @Override
    public String[] getVersion() {
        return delegate.getVersion();
    }

    @Override
    public void setSchema(Schema schema) {
        delegate.setSchema(schema);
    }

    @Override
    public void addSchema(Schema schema) throws EDISchemaException {
        delegate.addSchema(schema);
    }

    @Override
    public String getReferenceCode() {
        return delegate.getReferenceCode();
    }

    @Override
    public EDIStreamValidationError getErrorType() {
        return delegate.getErrorType();
    }

    @Override
    public String getText() {
        return delegate.getText();
    }

    @Override
    public char[] getTextCharacters() {
        return delegate.getTextCharacters();
    }

    @Override
    public int getTextCharacters(int sourceStart,
                                 char[] target,
                                 int targetStart,
                                 int length) {
        return delegate.getTextCharacters(sourceStart,
                                          target,
                                          targetStart,
                                          length);
    }

    @Override
    public int getTextStart() {
        return delegate.getTextStart();
    }

    @Override
    public int getTextLength() {
        return delegate.getTextLength();
    }

    @Override
    public Location getLocation() {
        return delegate.getLocation();
    }

    @Override
    public void setBinaryDataLength(long length) throws EDIStreamException {
        delegate.setBinaryDataLength(length);
    }

    @Override
    public InputStream getBinaryData() {
        return delegate.getBinaryData();
    }
}