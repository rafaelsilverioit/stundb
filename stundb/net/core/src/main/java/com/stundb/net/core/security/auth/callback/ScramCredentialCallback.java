/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stundb.net.core.security.auth.callback;

import com.stundb.net.core.models.auth.ScramCredential;

import javax.security.auth.callback.Callback;

/**
 * This class was taken from Apache Kafka
 * org.apache.kafka.common.security.scram.ScramCredentialCallback. It has been heavily modified for
 * the needs of this project.
 *
 * <p>Callback used for SCRAM mechanisms.
 */
public class ScramCredentialCallback implements Callback {
    private ScramCredential scramCredential;

    /** Sets the SCRAM credential for this instance. */
    public void scramCredential(ScramCredential scramCredential) {
        this.scramCredential = scramCredential;
    }

    /** Returns the SCRAM credential if set on this instance. */
    public ScramCredential scramCredential() {
        return scramCredential;
    }
}
