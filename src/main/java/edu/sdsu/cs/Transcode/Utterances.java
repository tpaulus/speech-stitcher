package edu.sdsu.cs.Transcode;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.google.gson.Gson;
import edu.sdsu.cs.Models.SourceClip;
import edu.sdsu.cs.Models.Word;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * @author Tom Paulus
 * Created on 2/28/18.
 */
@Log4j
public class Utterances {
    /**
     * Get a Word Utterance from the DynamoDB Table by the word associated with the utterance.
     *
     * @param tableName {@link String} Table name in DynamoDB
     * @param word      {@link String} Word to Search for in Dynamo
     * @return {@link Word[]} Matching Words (as array)
     */
    public static Word[] getWord(final String tableName, final String word) {
        // TODO Verify Multiple Words are returned from DDB
        // TODO Test Non-Existent
        return Dynamo.getInstance().getFromTable(tableName, "word", word, Word[].class);
    }

    /**
     * Get a Word Utterance from the DynamoDB Table based off of the utterance's unique ID.
     *
     * @param tableName {@link String} Table name in DynamoDB
     * @param id        {@link String} Utterance ID
     * @return {@link Word} Associated Word;null if the item doesn't exist.
     */
    public static Word getWordByID(final String tableName, final String id) {
        // TODO Test Non-Existent
        return Dynamo.getInstance().getFromTable(tableName, "id", id, Word.class);
    }

    public static FindResult getBestWordSequence(final String tableName, final List<String> wordSequence) throws UtteranceNotFoundException {
        Queue<ResultWord> wordsPQ = new PriorityQueue<>();

        // Load First word into the Queue
        ResultWord[] words = (ResultWord[]) getWord("tableName", wordSequence.get(0));
        if (words.length == 0) {
            throw new UtteranceNotFoundException(wordSequence.get(0));
        }

        for (ResultWord word : words) {
            word.calculateScore(tableName, wordSequence);
            wordsPQ.add(word);
        }

        ResultWord bestWord = wordsPQ.peek();
        log.info("Best Matching word has a score of " + bestWord.getScore());

        List<String> wordsMatched = new ArrayList<>();

        SourceClip clip = new SourceClip();

        Word currentWord = bestWord;
        int sequenceIndex = 0;
        Duration clipStart = Duration.between(
                LocalTime.MIN,
                LocalTime.parse(currentWord.getStart()));
        Duration clipEnd = clipStart;

        while (currentWord.getWord().equals(wordSequence.get(sequenceIndex))) {
            clipEnd = Duration.between(
                    LocalTime.MIN,
                    LocalTime.parse(currentWord.getEnd()));

            sequenceIndex++;
            currentWord = currentWord.getNextWord(tableName);
        }

        clip.setSourceArn(bestWord.getSource());
        clip.setStartTime(bestWord.getStart());
        clip.setDuration(DurationFormatUtils.formatDurationHMS(
                clipEnd.minus(clipStart).toMillis()));

        return FindResult.builder()
                .clip(clip)
                .wordsMatched(wordsMatched)
                .build();
    }

    public static class ResultWord extends Word implements Comparable<ResultWord> {
        @Getter
        private int score = 0;

        int calculateScore(final String tableName, final List<String> targetSequence) {
            if (getWord().equals(targetSequence.get(0)) && nextWord != null) {
                // If the current word matches, check if the next one does
                score = ((ResultWord) getNextWord(tableName)).calculateScore(tableName, targetSequence.subList(1, targetSequence.size())) + 1;
                return score;
            } else
                return 0;
        }

        @Override
        public int compareTo(@NotNull ResultWord o) {
            return score - o.score;
        }
    }

    @SuppressWarnings( {"unused", "WeakerAccess"})
    public static class UtteranceNotFoundException extends Exception {
        public UtteranceNotFoundException() {
            super("Could not find specified word in utterance table");
        }

        public UtteranceNotFoundException(final String word) {
            super(String.format("Could not find \"%s\" in utterance table", word));
        }
    }

    @AllArgsConstructor
    @Builder
    @Getter
    public static class FindResult {
        private List<String> wordsMatched;
        private SourceClip clip;
    }

    private static class Dynamo {
        @Getter
        private static Dynamo instance = new Dynamo();
        private DynamoDB dynamoDB;

        private Dynamo() {
            AmazonDynamoDB client = AmazonDynamoDBClientBuilder
                    .standard()
                    .withCredentials(Credentials.getCredentials())
                    .build();

            dynamoDB = new DynamoDB(client);
        }

        <T> T getFromTable(final String tableName,
                           final String keyName,
                           final String keyValue,
                           final Class T) {
            Table table = dynamoDB.getTable(tableName);
            Item item = table.getItem(keyName, keyValue);
            return new Gson().fromJson(item.toJSON(), (Type) T);
        }
    }
}
