/*
 * Copyright (C) 2018 - 2025 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.dynamic.task.mgt.core.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This executor ensures sequential execution of the task update requests.
 */
public class DynamicTaskContextPatchExecutor {
    private final ExecutorService singleThreadedExecutor;

    private DynamicTaskContextPatchExecutor() {
        singleThreadedExecutor = Executors.newSingleThreadExecutor();
    }

    public static DynamicTaskContextPatchExecutor getInstance() {
        return ReferenceHolder.INSTANCE;
    }

    /**
     * Patch and update the existing NTask specified by the {@link DynamicTaskPatch}
     *
     * @param dynamicTaskPatch {@link DynamicTaskPatch}
     */
    public void patch(DynamicTaskPatch dynamicTaskPatch) {
        singleThreadedExecutor.submit(dynamicTaskPatch);
    }

    private static class ReferenceHolder {
        public static DynamicTaskContextPatchExecutor INSTANCE = new DynamicTaskContextPatchExecutor();
    }
}
