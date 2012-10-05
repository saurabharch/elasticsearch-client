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

package org.elasticsearch.common.lucene;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.logging.ESLogger;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 *
 */
public class Lucene {

    public static final Version VERSION = Version.LUCENE_36;
    public static final Version ANALYZER_VERSION = VERSION;
    public static final Version QUERYPARSER_VERSION = VERSION;

    public static final int NO_DOC = -1;

    public static ScoreDoc[] EMPTY_SCORE_DOCS = new ScoreDoc[0];

    public static final int BATCH_ENUM_DOCS = 32;

    public static Version parseVersion(@Nullable String version, Version defaultVersion, ESLogger logger) {
        if (version == null) {
            return defaultVersion;
        }
        if ("3.6".equals(version)) {
            return Version.LUCENE_36;
        }
        if ("3.5".equals(version)) {
            return Version.LUCENE_35;
        }
        if ("3.4".equals(version)) {
            return Version.LUCENE_34;
        }
        if ("3.3".equals(version)) {
            return Version.LUCENE_33;
        }
        if ("3.2".equals(version)) {
            return Version.LUCENE_32;
        }
        if ("3.1".equals(version)) {
            return Version.LUCENE_31;
        }
        if ("3.0".equals(version)) {
            return Version.LUCENE_30;
        }
        logger.warn("no version match {}, default to {}", version, defaultVersion);
        return defaultVersion;
    }

    public static long count(IndexSearcher searcher, Query query) throws IOException {
        TotalHitCountCollector countCollector = new TotalHitCountCollector();
        // we don't need scores, so wrap it in a constant score query
        if (!(query instanceof ConstantScoreQuery)) {
            query = new ConstantScoreQuery(query);
        }
        searcher.search(query, countCollector);
        return countCollector.getTotalHits();
    }

    public static int docId(IndexReader reader, Term term) throws IOException {
        TermDocs termDocs = reader.termDocs(term);
        try {
            if (termDocs.next()) {
                return termDocs.doc();
            }
            return NO_DOC;
        } finally {
            termDocs.close();
        }
    }

    /**
     * Closes the index writer, returning <tt>false</tt> if it failed to close.
     */
    public static boolean safeClose(IndexWriter writer) {
        if (writer == null) {
            return true;
        }
        try {
            writer.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static Explanation readExplanation(StreamInput in) throws IOException {
        float value = in.readFloat();
        String description = in.readUTF();
        Explanation explanation = new Explanation(value, description);
        if (in.readBoolean()) {
            int size = in.readVInt();
            for (int i = 0; i < size; i++) {
                explanation.addDetail(readExplanation(in));
            }
        }
        return explanation;
    }

    public static void writeExplanation(StreamOutput out, Explanation explanation) throws IOException {
        out.writeFloat(explanation.getValue());
        out.writeUTF(explanation.getDescription());
        Explanation[] subExplanations = explanation.getDetails();
        if (subExplanations == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeVInt(subExplanations.length);
            for (Explanation subExp : subExplanations) {
                writeExplanation(out, subExp);
            }
        }
    }

    private static final Field segmentReaderSegmentInfoField;

    static {
        Field segmentReaderSegmentInfoFieldX = null;
        try {
            segmentReaderSegmentInfoFieldX = SegmentReader.class.getDeclaredField("si");
            segmentReaderSegmentInfoFieldX.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        segmentReaderSegmentInfoField = segmentReaderSegmentInfoFieldX;
    }

    public static SegmentInfo getSegmentInfo(SegmentReader reader) {
        try {
            return (SegmentInfo) segmentReaderSegmentInfoField.get(reader);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static class ExistsCollector extends Collector {

        private boolean exists;

        public void reset() {
            exists = false;
        }

        public boolean exists() {
            return exists;
        }

        @Override
        public void setScorer(Scorer scorer) throws IOException {
            this.exists = false;
        }

        @Override
        public void collect(int doc) throws IOException {
            exists = true;
        }

        @Override
        public void setNextReader(IndexReader reader, int docBase) throws IOException {
        }

        @Override
        public boolean acceptsDocsOutOfOrder() {
            return true;
        }
    }

    private Lucene() {

    }
}
