/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.device.mgt.core.report.mgt.util;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.NotFoundException;
import io.entgra.device.mgt.core.device.mgt.common.report.mgt.ChunkDescriptor;
import io.entgra.device.mgt.core.device.mgt.common.report.mgt.FileDescriptor;
import io.entgra.device.mgt.core.device.mgt.common.report.mgt.FileMetaEntry;
import io.entgra.device.mgt.core.device.mgt.core.report.mgt.exception.FileTransferServiceHelperUtilException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/**
 * Utility that manages the local artifact holders used while uploading category icons
 * for reports. This holds only the category icon related file transfer helpers that were
 * previously provided by the application management component, so that device management
 * core does not depend on the application management component.
 */
public class CategoryIconFileTransferUtil {
    private static final Log log = LogFactory.getLog(CategoryIconFileTransferUtil.class);
    private static final String META_ENTRY_FILE_NAME = ".meta.json";
    private static final String CATEGORY_ICON_ROOT = "category-icons";
    private static final String CARBON_HOME = "carbon.home";
    private static final Gson gson = new Gson();

    public static Path createCategoryIconArtifactHolder(FileMetaEntry fileMetaEntry)
            throws FileTransferServiceHelperUtilException {
        try {
            Path root = Paths.get(
                    System.getProperty(CARBON_HOME),
                    "repository", "resources", CATEGORY_ICON_ROOT
            );
            if (Files.notExists(root)) {
                setMinimumPermissions(Files.createDirectories(root));
            }
            Path artifactHolder = root.resolve(UUID.randomUUID().toString());
            if (Files.exists(artifactHolder)) {
                throw new FileTransferServiceHelperUtilException(
                        "Artifact holder already exists in " + artifactHolder);
            }
            setMinimumPermissions(Files.createDirectory(artifactHolder));
            createMetaEntry(fileMetaEntry, artifactHolder);
            createArtifactFile(fileMetaEntry, artifactHolder);
            return artifactHolder;
        } catch (IOException e) {
            String msg = "Error occurred while creating category icon artifact holder";
            log.error(msg, e);
            throw new FileTransferServiceHelperUtilException(msg, e);
        }
    }

    public static void populateCategoryIconChunkDescriptor(
            String artifactHolder,
            InputStream chunk,
            ChunkDescriptor chunkDescriptor)
            throws FileTransferServiceHelperUtilException, NotFoundException {

        Path root = Paths.get(
                System.getProperty(CARBON_HOME),
                "repository", "resources", CATEGORY_ICON_ROOT
        );
        Path holder = root.resolve(artifactHolder);

        if (Files.notExists(holder)) {
            throw new NotFoundException(
                    holder.toAbsolutePath() + " does not exist");
        }
        if (!Files.isDirectory(holder)) {
            throw new FileTransferServiceHelperUtilException(
                    holder.toAbsolutePath() + " is not a directory");
        }

        Path metaEntry = locateMetaEntry(holder);
        chunkDescriptor.setChunk(chunk);
        FileDescriptor fileDescriptor = new FileDescriptor();
        populateFileDescriptor(metaEntry, holder, fileDescriptor);
        chunkDescriptor.setAssociateFileDescriptor(fileDescriptor);
    }

    public static void writeChunk(ChunkDescriptor chunkDescriptor) throws FileTransferServiceHelperUtilException {
        if (chunkDescriptor == null) {
            throw new FileTransferServiceHelperUtilException("Received null for chuck descriptor");
        }
        FileDescriptor fileDescriptor = chunkDescriptor.getAssociateFileDescriptor();
        if (fileDescriptor == null) {
            throw new FileTransferServiceHelperUtilException("Target file descriptor is missing for retrieved chunk");
        }
        Path artifact = Paths.get(fileDescriptor.getAbsolutePath());
        try {
            InputStream chuckStream = chunkDescriptor.getChunk();
            byte []chunk = new byte[chuckStream.available()];
            chuckStream.read(chunk);
            Files.write(artifact, chunk, StandardOpenOption.CREATE, StandardOpenOption.SYNC, StandardOpenOption.APPEND);
        } catch (IOException e) {
            String msg = "Error encountered while writing to the " + artifact;
            log.error(msg, e);
            throw new FileTransferServiceHelperUtilException(msg, e);
        }
    }

    private static Path locateMetaEntry(Path artifactHolder) throws FileTransferServiceHelperUtilException {
        Path metaEntry = artifactHolder.resolve(META_ENTRY_FILE_NAME);
        if (Files.notExists(metaEntry) || Files.isDirectory(metaEntry)) {
            throw new FileTransferServiceHelperUtilException("Can't locate " + META_ENTRY_FILE_NAME);
        }

        if (!Files.isReadable(metaEntry)) {
            throw new FileTransferServiceHelperUtilException("Unreadable " + META_ENTRY_FILE_NAME);
        }
        return metaEntry;
    }

    private static void populateFileDescriptor(Path metaEntry, Path artifactHolder, FileDescriptor fileDescriptor)
            throws FileTransferServiceHelperUtilException {
        try {
            byte []metaEntryByteContent = Files.readAllBytes(metaEntry);
            FileMetaEntry fileMetaEntry = gson.fromJson(new String(metaEntryByteContent, StandardCharsets.UTF_8), FileMetaEntry.class);
            fileDescriptor.setFileName(fileMetaEntry.getFileName());
            fileDescriptor.setActualFileSize(fileMetaEntry.getSize());
            fileDescriptor.setFullQualifiedName(fileMetaEntry.getFileName() + "." + fileMetaEntry.getExtension());
            Path artifact = artifactHolder.resolve(fileDescriptor.getFullQualifiedName());
            fileDescriptor.setAbsolutePath(artifact.toAbsolutePath().toString());
            fileDescriptor.setExtension(fileMetaEntry.getExtension());
            fileDescriptor.setFile(Files.newInputStream(artifact));
        } catch (IOException e) {
            String msg = "Error encountered while populating chuck descriptor";
            log.error(msg, e);
            throw new FileTransferServiceHelperUtilException(msg, e);
        }
    }

    private static void createMetaEntry(FileMetaEntry fileMetaEntry, Path artifactHolder)
            throws FileTransferServiceHelperUtilException {
        try {
            Path metaEntry = artifactHolder.resolve(META_ENTRY_FILE_NAME);
            String fileMetaJsonContent = gson.toJson(fileMetaEntry);
            Files.write(metaEntry, fileMetaJsonContent.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.SYNC);
        } catch (IOException e) {
            throw new FileTransferServiceHelperUtilException("Error encountered while creating meta entry", e);
        }
    }

    private static void createArtifactFile(FileMetaEntry fileMetaEntry, Path artifactHolder)
            throws FileTransferServiceHelperUtilException {
        try {
            Path artifactFile = artifactHolder.resolve(fileMetaEntry.getFileName() + "." + fileMetaEntry.getExtension());
            fileMetaEntry.setAbsolutePath(artifactFile.toAbsolutePath().toString());
            Files.createFile(artifactFile);
            setMinimumPermissions(artifactFile);
        } catch (IOException e) {
            throw new FileTransferServiceHelperUtilException("Error encountered while creating artifact file", e);
        }
    }

    private static void setMinimumPermissions(Path path) throws FileTransferServiceHelperUtilException {
        File file = path.toFile();
        if (!file.setReadable(true, true)) {
            throw new FileTransferServiceHelperUtilException("Failed to set read permission for " + file.getAbsolutePath());
        }

        if (!file.setWritable(true, true)) {
            throw new FileTransferServiceHelperUtilException("Failed to set write permission for " + file.getAbsolutePath());
        }
    }
}
