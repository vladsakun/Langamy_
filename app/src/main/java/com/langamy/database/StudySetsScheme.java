package com.langamy.database;

public class StudySetsScheme {

    public static final class StudySetsTable {

        public static final String NAME = "api_studysets";

        public static final class Cols {

            public static final String id = "_id";
            public static final String name = "name";
            public static final String language_to = "language_to";
            public static final String language_from = "language_from";
            public static final String words = "words";
            public static final String creator = "creator";
            public static final String percent_of_studying = "percent_of_studying";
            public static final String studied = "studied";
            public static final String amount_of_words = "amount_of_words";
            public static final String marked_words = "marked_words";
            public static final String sync_status = "sync_status";
        }

    }
}
