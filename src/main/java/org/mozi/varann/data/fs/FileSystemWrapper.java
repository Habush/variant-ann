/*
 * Disq
 *
 * MIT License
 *
 * Copyright (c) 2018-2019 Disq contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.mozi.varann.data.fs;

import htsjdk.samtools.seekablestream.SeekableStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public interface FileSystemWrapper extends Serializable {
    class HiddenFileFilter implements Predicate<String> {
        @Override
        public boolean test(String path) {
            return !(FilenameUtils.getBaseName(path).startsWith(".")
                    || FilenameUtils.getBaseName(path).startsWith("_"));
        }
    }
    /** Represents a file in a directory listing. */
    class FileStatus implements Comparable<FileStatus> {
        private static final Comparator<FileStatus> COMPARATOR =
                Comparator.comparing(FileStatus::getPath, Comparator.nullsFirst(String::compareTo))
                        .thenComparingLong(FileStatus::getLength);

        private final String path;
        private final long length;

        public FileStatus(String path, long length) {
            this.path = path;
            this.length = length;
        }

        /** @return the file path */
        public String getPath() {
            return path;
        }

        /** @return the length of the file in bytes */
        public long getLength() {
            return length;
        }

        @Override
        public int compareTo(FileStatus o) {
            return COMPARATOR.compare(this, o);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FileStatus that = (FileStatus) o;
            return length == that.length && path.equals(that.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, length);
        }
    }

    /** @return true if this implementation uses the {@link java.nio} API */
    boolean usesNio();

    /**
     * Returns a consistent, fully-qualified representation of the path.
     * @param path the path to the file to open
     * @return the normalized (fully-qualified) path
     * @throws IOException if an IO error occurs
     */
    String normalize(String path) throws IOException;

    /**
     * Open a file for reading. The caller is responsible for closing the stream that is returned.
     *
     * @param path the path to the file to open
     * @return a seekable stream to read from
     * @throws IOException if an IO error occurs
     */
    SeekableStream open(String path) throws IOException;

    /**
     * Create a file for writing, overwriting an existing file. The caller is responsible for closing
     * the stream that is returned.
     *
     * @param path the path to the file to create
     * @return a stream to write to
     * @throws IOException if an IO error occurs
     */
    OutputStream create(String path) throws IOException;

    /**
     * Delete a file or directory.
     *
     * @param path the path to the file or directory to delete
     * @return true if the file or directory was successfully deleted, false if the path didn't exist
     * @throws IOException if an IO error occurs
     */
    boolean delete(String path) throws IOException;

    /**
     * Check if a file or directory exists.
     *
     * @param path the path to the file or directory to check for existence
     * @return true if the specified path represents a file or directory in the filesystem
     * @throws IOException if an IO error occurs
     */
    boolean exists(String path) throws IOException;

    /**
     * Returns the size of a file, in bytes.
     *
     * @param path the path to the file
     * @return the file size, in bytes
     * @throws IOException if an IO error occurs
     */
    long getFileLength(String path) throws IOException;

    /**
     * Check if a path is a directory.
     *
     * @param path the path to check
     * @return true if the specified path represents a directory in the filesystem
     * @throws IOException if an IO error occurs
     */
    boolean isDirectory(String path) throws IOException;

    /**
     * Return the paths of files in a directory, in lexicographic order.
     *
     * @param path the path to the directory
     * @return paths in lexicographic order
     * @throws IOException if an IO error occurs
     */
    List<String> listDirectory(String path) throws IOException;

    /**
     * Return the file statuses of files in a directory, in lexicographic order of the paths.
     *
     * @param path the path to the directory
     * @return file statuses in lexicographic order of the paths
     * @throws IOException if an IO error occurs
     */
    List<FileStatus> listDirectoryStatus(String path) throws IOException;

    /**
     * Concatenate the contents of multiple files into a single file.
     *
     * @param parts the paths of files to concatenate
     * @param path the path of the output file
     * @throws IOException if an IO error occurs
     */
    void concat(List<String> parts, String path) throws IOException;

    default String firstFileInDirectory(String path) throws IOException {
        Optional<String> firstPath =
                listDirectory(path).stream().filter(new HiddenFileFilter()).findFirst();
        if (!firstPath.isPresent()) {
            throw new IllegalArgumentException("No files found in " + path);
        }
        return firstPath.get();
    }
}