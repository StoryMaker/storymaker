package org.storymaker.app.model.template;

import timber.log.Timber;

public class Clip {

    // TODO use getter/setters
    public String mTitle;
    public String mArtwork;
    public String mShotSize;
    public int mShotType = -1;
    public String mGoal;
    public String mLength;
    public String mDescription;
    public String mTip;
    public String mSecurity;
    
    public Clip ()
    {
        
    }
    
    public void setDefaults()
    {
        mDescription="Your clip";
        mShotType = 3;
        mArtwork = "cliptype_medium";
        mTip = "Add your own shot";
    }
    /*
    "Clip": "Signature",
    "Artwork": "???.svg",
    "Shot Size": "Detail",
    "Goal": "Depict something noteworthy about the character.",
    "Length": "X seconds",
    "Description": "What does the character do for a living? Show the audience the most important element of the character. This element should fill at least 50% of the frame.",
    "Tip": "If you are too close to the action, your phone’s camera may not be able to focus. Keep the camera at arm’s length from your subject.",
    "Security Concern": "-
    */
}
