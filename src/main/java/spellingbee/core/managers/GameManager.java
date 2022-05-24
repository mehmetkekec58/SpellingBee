package spellingbee.core.managers;

import spellingbee.core.constants.Messages;
import spellingbee.core.data.DataFilter;
import spellingbee.core.data.DataReader;
import spellingbee.core.data.FilteredData;
import spellingbee.core.data.GameData;
import spellingbee.core.exceptions.IllegalPointRangeException;
import spellingbee.core.exceptions.NotEnoughWordsException;
import spellingbee.core.exceptions.PangramNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameManager implements GameService {

    private GameData gameData;
    private DataReader dataReader;
    private DataFilter dataFilter;
    private List<String> selectedWords;
    private Random r;

    public GameManager(DataFilter dataFilter, DataReader dataReader) {
        this.dataFilter = dataFilter;
        this.dataReader = dataReader;

        selectedWords = new ArrayList<>();
        r = new Random();
    }

    public void create(String letters) throws PangramNotFoundException, NotEnoughWordsException, IllegalPointRangeException {

        FilteredData filteredData = filterWords(letters);

        firstStatusChecks(filteredData);

        List<String> filteredWords = filteredData.getWords();
        List<String> pangramWords = filteredData.getPangramWords();

        selectedWords.add(selectRandomPangram(pangramWords));

        secondStatusChecks(filteredWords, pangramWords);

        if (startTheGame(filteredWords, pangramWords)) {
            gameData = new GameData(filteredWords, pangramWords, letters);
            return;
        }
        gameData = prepareTheGame(filteredData, filteredWords, pangramWords, letters);

    }

    public void create() throws PangramNotFoundException, NotEnoughWordsException, IllegalPointRangeException {
        FilteredData filteredData = filterWords();

        firstStatusChecks(filteredData);
        String letters = filteredData.getLetters();
        List<String> filteredWords = filteredData.getWords();
        List<String> pangramWords = filteredData.getPangramWords();

        selectedWords.add(selectRandomPangram(pangramWords));

        secondStatusChecks(filteredWords, pangramWords);

        if (startTheGame(filteredWords, pangramWords)) {
            gameData = new GameData(filteredWords, pangramWords, letters);
            return;
        }
        gameData = prepareTheGame(filteredData, filteredWords, pangramWords, letters);
    }

    private boolean totalPointAcceptable(int totalPoint) {
        return totalPoint >= 100 && totalPoint <= 400;
    }

    private boolean isPangram(List<String> pangramWords, String word) {
        return pangramWords.contains(word);
    }

    private int getWordPoint(List<String> pangramWords, String word) {
        int sum = 0;
        if (!isPangram(pangramWords, word)) {
            sum += word.length() - 3;
        } else {
            sum += word.length() + 4;
        }
        return sum;
    }

    private int filteredWordsTotal(List<String> pangramWords, List<String> words) {
        int totalPoint = 0;
        for (String word : words) {
            totalPoint += getWordPoint(pangramWords, word);
        }
        return totalPoint;
    }

    private GameData prepareTheGame(FilteredData filteredData, List<String> filteredWords, List<String> pangramWords, String letters) {
        List<String> newPangrams = new ArrayList<>();
        int totalPoint = 0;
        while (!totalPointAcceptable(totalPoint)) {
            int wordCount = 20 + r.nextInt(Math.min(60, filteredData.getWords().size() - 20));
            totalPoint = 0;
            selectedWords.clear();
            newPangrams.clear();
            while (selectedWords.size() < wordCount) {
                String randomWord = filteredWords.get(r.nextInt(filteredWords.size()));
                if (!selectedWords.contains(randomWord)) {
                    selectedWords.add(randomWord);
                    if (pangramWords.contains(randomWord)) {
                        newPangrams.add(randomWord);
                    }
                }
            }
            for (String word : selectedWords) {
                totalPoint += getWordPoint(pangramWords, word);
            }
        }
        return new GameData(selectedWords, newPangrams, letters);
    }

    private void firstStatusChecks(FilteredData filteredData) throws PangramNotFoundException, NotEnoughWordsException {
        if (filteredData.getPangramWords().size() == 0) {
            throw new PangramNotFoundException(Messages.PANGRAM_NOT_FOUND);
        }
        if (filteredData.getWords().size() < 20) {
            throw new NotEnoughWordsException(Messages.NOT_ENOUGH_WORDS);
        }
    }

    private void secondStatusChecks(List<String> filteredWords, List<String> pangramWords) throws IllegalPointRangeException {
        if (filteredWordsTotal(pangramWords, filteredWords) < 100) {
            throw new IllegalPointRangeException(Messages.ILLEGAL_POINT_RANGE);
        }
    }

    private boolean startTheGame(List<String> filteredWords, List<String> pangramWords) {
        return filteredWordsTotal(pangramWords, filteredWords) < 400 && filteredWords.size() <= 80;
    }

    private String selectRandomPangram(List<String> pangramWords) {
        return pangramWords.get(r.nextInt(pangramWords.size()));
    }

    private FilteredData filterWords(String letters) {
        return dataFilter.filter(readWords(), letters);
    }

    private FilteredData filterWords() {
        return dataFilter.filter(readWords());
    }

    private List<String> readWords() {
        return dataReader.read();
    }

}



