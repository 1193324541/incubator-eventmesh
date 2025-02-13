/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.eventmesh.runtime.core.protocol.tcp.client.task;

import static org.apache.eventmesh.common.protocol.tcp.Command.LISTEN_RESPONSE;

import org.apache.eventmesh.common.protocol.tcp.Header;
import org.apache.eventmesh.common.protocol.tcp.OPStatus;
import org.apache.eventmesh.common.protocol.tcp.Package;
import org.apache.eventmesh.runtime.boot.EventMeshTCPServer;

import io.netty.channel.ChannelHandlerContext;

public class ListenTask extends AbstractTask {

    public ListenTask(Package pkg, ChannelHandlerContext ctx, long startTime, EventMeshTCPServer eventMeshTCPServer) {
        super(pkg, ctx, startTime, eventMeshTCPServer);
    }

    @Override
    public void run() {
        long taskExecuteTime = System.currentTimeMillis();
        Header header = new Header(LISTEN_RESPONSE, OPStatus.SUCCESS.getCode(), OPStatus.SUCCESS.getDesc(), pkg.getHeader().getSeq());
        session.setListenRequestSeq(pkg.getHeader().getSeq());
        try {
            synchronized (session) {
                eventMeshTCPServer.getClientSessionGroupMapping().readySession(session);
            }
        } catch (Exception e) {
            logger.error("ListenTask failed|user={}|errMsg={}", session.getClient(), e);
            Integer status = OPStatus.FAIL.getCode();
            header = new Header(LISTEN_RESPONSE, status, e.toString(), pkg.getHeader().getSeq());
        } finally {
            //check to avoid send repeatedly
            session.trySendListenResponse(header, startTime, taskExecuteTime);
        }
    }
}
