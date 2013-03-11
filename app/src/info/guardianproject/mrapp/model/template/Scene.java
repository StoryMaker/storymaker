package info.guardianproject.mrapp.model.template;

import java.util.ArrayList;

public class Scene {

    public String mTitle;
    public String mDescription;
    ArrayList<Clip> mArrayClips;
    
    public Scene()
    {
        
    }
    
    public void setDefaults()
    {
        mTitle="Your scene";
        mDescription="Scene description.";
    }
    
    public ArrayList<Clip> getClips()
    {
        return mArrayClips;
    }
    
    public Clip getClip(int idx) {
        return mArrayClips.get(idx);
    }

    public void addClip(Clip clip)
    {
        if (mArrayClips == null) {
            mArrayClips = new ArrayList<Clip>();
        }
        
        mArrayClips.add(clip);
    }
    
    public void setClips (ArrayList<Clip> clips)
    {
    	mArrayClips = clips;
    }
}