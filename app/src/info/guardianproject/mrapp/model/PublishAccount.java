package info.guardianproject.mrapp.model;

public class PublishAccount{
	
	private int Id;
	private String Name;
	private String IconUrl;
	private boolean IsConnected;
	
	public PublishAccount(int id, String name, String iconUrl, boolean isConnected){
		this.Id = id;
		this.Name = name;
		this.IconUrl = iconUrl;
		this.IsConnected = isConnected;
	}
	
	public int getId(){
	     return this.Id;
	}
	public void setId(int id){
	     this.Id = id;
	}
	
	public String getName(){
	     return this.Name;
	}
	public void setName(String name){
	     this.Name = name;
	}
	
	public String getIconUrl(){
	     return this.IconUrl;
	}
	public void setId(String iconUrl){
	     this.IconUrl = iconUrl;
	}
	
	public boolean getIsConnected(){
	     return this.IsConnected;
   	}
   	public void setIsConnected(boolean isConnected){
   	     this.IsConnected = isConnected;
   	} 	
}
