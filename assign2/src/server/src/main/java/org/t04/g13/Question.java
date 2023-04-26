package server.src.main.java.org.t04.g13;

public class Question {
    private String question;
    private String[] choices;
    private String answer;

    public Question(String question, String answer1, String answer2, String answer3, String answer4, String answer) {
        this.question = question;
        this.choices = new String[]{answer1, answer2, answer3, answer4};
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public String[] getAnswers() {
        return choices;
    }

    public String getCorrectAnswer() {
        return answer;
    }
}