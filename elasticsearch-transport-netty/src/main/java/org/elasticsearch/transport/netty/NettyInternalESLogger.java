/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.transport.netty;

import org.elasticsearch.common.logging.ESLogger;
import org.jboss.netty.logging.AbstractInternalLogger;

/**
 *
 */
public class NettyInternalESLogger extends AbstractInternalLogger {

    private final ESLogger logger;

    public NettyInternalESLogger(ESLogger logger) {
        this.logger = logger;
    }

    
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    
    public void debug(String msg) {
        logger.debug(msg);
    }

    
    public void debug(String msg, Throwable cause) {
        logger.debug(msg, cause);
    }

    
    public void info(String msg) {
        logger.info(msg);
    }

    
    public void info(String msg, Throwable cause) {
        logger.info(msg, cause);
    }

    
    public void warn(String msg) {
        logger.warn(msg);
    }

    
    public void warn(String msg, Throwable cause) {
        logger.warn(msg, cause);
    }

    
    public void error(String msg) {
        logger.error(msg);
    }

    
    public void error(String msg, Throwable cause) {
        logger.error(msg, cause);
    }
}
