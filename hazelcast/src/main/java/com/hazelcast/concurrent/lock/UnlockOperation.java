/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.concurrent.lock;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.spi.BackupAwareOperation;
import com.hazelcast.spi.Notifier;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.WaitNotifyKey;

import java.io.IOException;

public class UnlockOperation extends BaseLockOperation implements Notifier, BackupAwareOperation {

    private boolean force = false;

    public UnlockOperation() {
    }

    public UnlockOperation(ILockNamespace namespace, Data key, int threadId) {
        super(namespace, key, threadId);
    }

    public UnlockOperation(ILockNamespace namespace, Data key, int threadId, boolean force) {
        super(namespace, key, threadId);
        this.force = force;
    }

    public void run() throws Exception {
        if (force) {
            response = getLockStore().forceUnlock(key);
        } else {
            response = getLockStore().unlock(key, getCallerUuid(), threadId);
        }
    }

    public Operation getBackupOperation() {
        return new UnlockBackupOperation(namespace, key, threadId, getCallerUuid(), force);
    }

    public boolean shouldBackup() {
        return response;
    }

    public boolean shouldNotify() {
        return response;
    }

    public final WaitNotifyKey getNotifiedKey() {
        return new LockWaitNotifyKey(namespace, key);
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeBoolean(force);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        force = in.readBoolean();
    }
}
