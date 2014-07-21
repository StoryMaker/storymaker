package org.codeforafrica.listeningpost.lessons;

public interface LessonManagerListener {

    public void lessonLoadingStatusMessage(String msg);

    public void loadingLessonFromServer(String subFolder, String lessonTitle);

    public void lessonsLoadedFromServer();

    public void errorLoadingLessons(String msg);
}
