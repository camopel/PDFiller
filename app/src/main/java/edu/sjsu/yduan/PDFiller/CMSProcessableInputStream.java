package edu.sjsu.yduan.PDFiller;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.tom_roush.pdfbox.io.IOUtils;
import org.spongycastle.asn1.cms.CMSObjectIdentifiers;
import org.spongycastle.cms.CMSException;
import org.spongycastle.cms.CMSTypedData;
import org.spongycastle.asn1.ASN1ObjectIdentifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class CMSProcessableInputStream implements CMSTypedData
{
    private InputStream in;
    private final ASN1ObjectIdentifier contentType;

    CMSProcessableInputStream(InputStream is)
    {
        this(new ASN1ObjectIdentifier(CMSObjectIdentifiers.data.getId()), is);
    }

    CMSProcessableInputStream(ASN1ObjectIdentifier type, InputStream is)
    {
        contentType = type;
        in = is;
    }

    @Override
    public Object getContent()
    {
        return in;
    }

    @Override
    public void write(OutputStream out) throws IOException, CMSException
    {
        // read the content only one time
        IOUtils.copy(in, out);
        in.close();
    }

    @Override
    public ASN1ObjectIdentifier getContentType()
    {
        return contentType;
    }
}