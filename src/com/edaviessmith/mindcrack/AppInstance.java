package com.edaviessmith.mindcrack;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.edaviessmith.mindcrack.data.Member;
import com.edaviessmith.mindcrack.data.Post;
import com.edaviessmith.mindcrack.data.Tweet;
import com.edaviessmith.mindcrack.data.YoutubeItem;
import com.edaviessmith.mindcrack.db.MemberORM;
import com.edaviessmith.mindcrack.db.RedditORM;
import com.edaviessmith.mindcrack.db.TwitterORM;
import com.edaviessmith.mindcrack.db.YoutubeItemORM;

public class AppInstance extends Application{

	private static Context context;
	private static String TAG = "AppInstance";
	
	//Members Activity
    public static Member currentMember;
    public static List<Member> mindcrackers; //Sidemenu
    public static List<Member> allMembers; //Manage members (any status)
	

	//Fragment Variables
	public static int currentYoutubeMemberId = -1;		
	public static List<YoutubeItem> youtubeItems;
	public static String playlistPageToken = "";
	public static boolean isYoutubeItemsUpToDate;
	
	public static int currentTwitterMemberId = -1;
	public static List<Tweet> twitterFeed;
	public static int twitterPageToken;
	public static boolean isTwitterFeedUpToDate;
	
	public static List<Post> redditFeed;
	public static String redditPageToken;
	public static boolean isRedditFeedUpToDate;
	
	public static String redditPost;
	
	//Update booleans
	public static boolean isMindcrackersUpToDate;
    public static boolean isAllMembersUpToDate;
    

    
	// CONTEXT
    public void onCreate(){
        super.onCreate();
        AppInstance.context = getApplicationContext();
    }

    public static Context getContext() {
        return AppInstance.context;
    }
	

	// MEMBERS
	public static List<Member> getMindcrackers() {
		if(mindcrackers == null || mindcrackers.size() == 0 || !isMindcrackersUpToDate) {
			mindcrackers = MemberORM.getVisibleMembers(getContext());
			isMindcrackersUpToDate = true;
			
			Log.d("APP","member list changed to up to date");
		}
		return mindcrackers;
	}
	
	public static List<Member> getAllMembers() {
		if(allMembers == null || allMembers.size() == 0 || !isAllMembersUpToDate) {
			allMembers = MemberORM.getMembers(getContext());
			isAllMembersUpToDate = true;
			
			Log.d("APP","all list changed to up to date");
		}
		return allMembers;
	}
	
	
	public static Member getMember() {
		if(currentMember == null) {
			SharedPreferences settings = getContext().getSharedPreferences(Constants.PREFS, 0);
		    int memberId = settings.getInt(Constants.PREF_MEMBER, -1);
		    //none set so make it random :D
		    if(memberId == -1) {
		    	currentMember = null;
		    	//Random rnd = new Random();
		    	//memberId = rnd.nextInt(getMindcrackers().size());
		    } else {
			    for(Member member : getMindcrackers()) {
		        	if(member.getId() == memberId)
		        		currentMember = member;
		        }
		    }
		    //currentMember = getMindcrackers().get(memberId);
		}
		return currentMember;
	}
	
	@SuppressLint("NewApi")
	public static void setMember(int memberId) {
		SharedPreferences.Editor settings = getContext().getSharedPreferences(Constants.PREFS, 0).edit();
        settings.putInt(Constants.PREF_MEMBER,  memberId);
        if (android.os.Build.VERSION.SDK_INT >= 9) {
        	settings.apply();
        } else {
        	settings.commit();
        }
        //Member currently set
        if(memberId == -1) {
        	currentMember = null;
        }
        if(getMember() == null || memberId != getMember().getId()) {
	        for(Member member : getMindcrackers()) {
	        	if(member.getId() == memberId)
	        		currentMember = member;
	        }
	        isYoutubeItemsUpToDate = false;
	        isTwitterFeedUpToDate = false;
        }
	}
	
	//// YOUTUBE ////
    
	public static int getCurrentYoutubeMemberId() {
    	if(currentYoutubeMemberId == -1) {
    		setCurrentYoutubeMemberId(AppInstance.getMember().getId());
    	}
		return currentYoutubeMemberId;
	}

	public static void setCurrentYoutubeMemberId(int currentFragmentMemberId) {
		AppInstance.currentYoutubeMemberId = currentFragmentMemberId;
	}
	
    public static boolean checkSetYoutubeMemberIdIsCurrent() {
    	boolean same = AppInstance.currentYoutubeMemberId == getMember().getId();
    	setCurrentYoutubeMemberId(AppInstance.getMember().getId());    	
    	return same;    	
	}
	
	public static List<YoutubeItem> getYoutubeItems() {
		if(youtubeItems == null || youtubeItems.size() == 0 || !isYoutubeItemsUpToDate) {
			Log.d(TAG, "getting youtube items from db");
			youtubeItems = YoutubeItemORM.getMemberYoutubeItems(getContext(), getMember().getId());
			isYoutubeItemsUpToDate = true;
		}
		return youtubeItems;
	}
	
	//Update YoutubeItems
    public static void updateYoutubeItems(List<YoutubeItem> newYoutubeItems){

    	//ORM get latest videoid
    	YoutubeItem latestYoutubeItem = YoutubeItemORM.getMemberLatestYoutubeItem(getContext(), getMember().getId());
    	
    	
    	if(latestYoutubeItem == null) {
    		YoutubeItemORM.insertMemberYoutubeItems(getContext(), newYoutubeItems);
    		Log.d(TAG, "adding youtube items");
    		isYoutubeItemsUpToDate = false;
    	}else {
    		//Compare the first NewYoutubeItem video with the latest
    		if(!newYoutubeItems.get(0).getVideoId().equals(latestYoutubeItem.getVideoId())) {
    			YoutubeItemORM.updateMemberYoutubeItems(getContext(), newYoutubeItems);
    			isYoutubeItemsUpToDate = false;
    			Log.d(TAG, "updating youtube items");
    		} else {
    			Log.d(TAG, "youtube items already up to date");
    			isYoutubeItemsUpToDate = true;
    		}
    	}
    }
    
    
    //// TWITTER ////
    
    
    public static int getCurrentTwitterMemberId() {
    	if(currentTwitterMemberId == -1) {
    		setCurrentTwitterMemberId(AppInstance.getMember().getId());
    	}
		return currentTwitterMemberId;
	}

	public static void setCurrentTwitterMemberId(int currentFragmentMemberId) {
		AppInstance.currentTwitterMemberId = currentFragmentMemberId;
	}
	
    public static boolean checkSetTwitterMemberIdIsCurrent() {
    	boolean same = AppInstance.currentTwitterMemberId == getMember().getId();
    	setCurrentTwitterMemberId(AppInstance.getMember().getId());    	
    	return same;    	
	}
    
	public static List<Tweet> getTwitterFeed() {
		if(twitterFeed == null || twitterFeed.size() == 0 || !isTwitterFeedUpToDate) {
			Log.d(TAG, "getting twitter feed from db");
			twitterFeed = TwitterORM.getMemberTwitterFeed(getContext(), getMember().getId());
			isTwitterFeedUpToDate = true;
		}
		return twitterFeed;
	}
    
	public static void updateTwitterFeed(List<Tweet> tweets) {
		//ORM get latest tweet
    	Tweet latestTweet = TwitterORM.getMemberLatestTwitterFeed(getContext(), getMember().getId());
    	
    	
    	if(latestTweet == null) {
    		TwitterORM.insertMemberTwitterFeed(getContext(), tweets);
    		Log.d(TAG, "adding youtube items");
    		isTwitterFeedUpToDate = false;
    	}else {
    		//Compare the first NewYoutubeItem video with the latest
    		if(tweets.get(0).getTweetId() != latestTweet.getTweetId()) {
    			TwitterORM.updateMemberTwitterFeed(getContext(), tweets);
    			isTwitterFeedUpToDate = false;
    			Log.d(TAG, "updating twitter feed");
    		} else {
    			Log.d(TAG, "twitter feed already up to date");
    			isTwitterFeedUpToDate = true;
    		}
    	}
	}
	
	
	//// REDDIT ////
	
	//TODO go to database for reddit
	public static List<Post> getRedditFeed() {
		if(redditFeed == null || redditFeed.size() == 0 || !isRedditFeedUpToDate) {
			Log.d(TAG, "getting reddit feed from db");
			redditFeed = RedditORM.getRedditFeed(getContext());
			isRedditFeedUpToDate = true;
		}
		return redditFeed;
	}
    
	public static void updateRedditFeed(List<Post> posts) {
		//ORM get latest tweet
    	Post latestPost = RedditORM.getLatestRedditFeed(getContext());
    	
    	
    	if(latestPost == null) {
    		RedditORM.insertRedditFeed(getContext(), posts);
    		Log.d(TAG, "adding youtube items");
    		isRedditFeedUpToDate = false;
    	}else {
    		//Compare the first NewYoutubeItem video with the latest
    		if(posts.get(0).getId() != latestPost.getId()) {
    			RedditORM.insertRedditFeed(getContext(), posts);
    			isRedditFeedUpToDate = false;
    			Log.d(TAG, "updating reddit feed");
    		} else {
    			Log.d(TAG, "reddit feed already up to date");
    			isRedditFeedUpToDate = true;
    		}
    	}
	}
	
	
	//// MEMBER ////
	
	//Update single Member from Members
    public static void updateCurrentMember(){
    	MemberORM.updateMember(getContext(), getMember());
    	isAllMembersUpToDate = false;
    }
    
	//Update all Members from MamnageMember
    public static void updateMembers(List<Member> members){

    	for(int i = 0; i < members.size(); i++) {
    		members.get(i).setSort(i);
    		Log.d("Data",members.get(i).toDatabaseString());
    	}
    	
    	MemberORM.updateMembers(getContext(), members);
    	isMindcrackersUpToDate = false;
    	isAllMembersUpToDate = false;
    }


	

}
