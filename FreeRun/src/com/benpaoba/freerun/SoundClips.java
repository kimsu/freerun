package com.benpaoba.freerun;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

/*
 * This class controls the sound playback according to the API level.
 */
public class SoundClips {
    // Sound actions.
    public static final int START_SPORT = 0;
    public static final int PAUSE_SPORT = 1;
    public static final int CONTINUE_SPORT = 2;
    public static final int COMPLETE_SPORT = 3;
    public static final int TIMETICK_EACHMILE_SPORT = 4;
    
    public static final int TIMETICK_DIDONG = 5;
    public static final int TIMETICK_YOU_HAVE_ALREADY = 6;
    public static final int TIMETICK_RUN = 7;
    public static final int TIMETICK_KILOMETER = 8;
    public static final int TIMETICK_NEARBYONEMILE = 9;
    public static final int TIMETICK_SPENDTIME = 10;
    
    private static final int MILLISECONDS_INTERVAL = 600;

    private boolean isPlaying = false;
    public interface Player {
        public void release();
        public void play(int action, int totalMiles, long totalSeconds, long nearByOneMileTime);
        public boolean isPlaying();
    }

    public static Player getPlayer(Context context) {
        return new SoundPoolPlayer(context);
    }

    /**
     * This class implements SoundClips.Player using SoundPool, which
     * exists since API level 1.
     */
    private static class SoundPoolPlayer implements
            Player, SoundPool.OnLoadCompleteListener {

        private static final String TAG = "SoundPoolPlayer";
        private static final int NUM_SOUND_STREAMS = 1;

        private HashMap<Integer, Integer> mSoundResMap;
        private HashMap<Integer,Boolean> mSoundIDReady;
        // ID returned by load() should be non-zero.
        private static final int ID_NOT_LOADED = 0;

        // Maps a sound action to the id;
        private final int[] mActions = {0, //START_SPORT
        		1, //PAUSE_SPORT
        		2, //CONTINUE_SPORT
        		3, //COMPLETE_SPORT
        		4 //TIMETICK_EACHMILE_SPORT
        };
        // Store the context for lazy loading.
        private Context mContext;
        // mSoundPool is created every time load() is called and cleared every
        // time release() is called.
        private SoundPool mSoundPool;
        private int mSoundIDToPlay;

        private void initializAllResources() {
        	mSoundResMap = new HashMap<Integer, Integer>();
        	mSoundResMap.put(R.raw.sport_start, 0);
        	mSoundResMap.put(R.raw.sport_pause, 0);
        	mSoundResMap.put(R.raw.sport_continue, 0);
        	mSoundResMap.put(R.raw.sport_completed, 0);
        	mSoundResMap.put(R.raw.dingdong, 0);
        	mSoundResMap.put(R.raw.you_have_already, 0);
        	mSoundResMap.put(R.raw.run, 0);
        	mSoundResMap.put(R.raw.kilometer, 0);
        	mSoundResMap.put(R.raw.spend_time, 0);
        	mSoundResMap.put(R.raw.nearbyonemile, 0);
        	mSoundResMap.put(R.raw.minute, 0);
        	mSoundResMap.put(R.raw.second, 0);
        	mSoundResMap.put(R.raw.one, 0);
        	mSoundResMap.put(R.raw.two, 0);
        	mSoundResMap.put(R.raw.three, 0);
        	mSoundResMap.put(R.raw.four, 0);
        	mSoundResMap.put(R.raw.five, 0);
        	mSoundResMap.put(R.raw.six, 0);
        	mSoundResMap.put(R.raw.seven, 0);
        	mSoundResMap.put(R.raw.eight, 0);
        	mSoundResMap.put(R.raw.nine, 0);
        	mSoundResMap.put(R.raw.zero, 0);
        	mSoundResMap.put(R.raw.ten, 0);
        	mSoundResMap.put(R.raw.hundred, 0);
        	mSoundResMap.put(R.raw.come_on, 0);
        	
        	mSoundIDReady = new HashMap<Integer, Boolean>();
        	mSoundIDReady.put(R.raw.sport_start, false);
        	mSoundIDReady.put(R.raw.sport_pause, false);
        	mSoundIDReady.put(R.raw.sport_continue, false);
        	mSoundIDReady.put(R.raw.sport_completed, false);
        	mSoundIDReady.put(R.raw.dingdong, false);
        	mSoundIDReady.put(R.raw.you_have_already, false);
        	mSoundIDReady.put(R.raw.run, false);
        	mSoundIDReady.put(R.raw.kilometer, false);
        	mSoundIDReady.put(R.raw.spend_time, false);
        	mSoundIDReady.put(R.raw.nearbyonemile, false);
        	mSoundIDReady.put(R.raw.minute, false);
        	mSoundIDReady.put(R.raw.second, false);
        	mSoundIDReady.put(R.raw.one, false);
        	mSoundIDReady.put(R.raw.two, false);
        	mSoundIDReady.put(R.raw.three, false);
        	mSoundIDReady.put(R.raw.four, false);
        	mSoundIDReady.put(R.raw.five, false);
        	mSoundIDReady.put(R.raw.six, false);
        	mSoundIDReady.put(R.raw.seven, false);
        	mSoundIDReady.put(R.raw.eight, false);
        	mSoundIDReady.put(R.raw.nine, false);
        	mSoundIDReady.put(R.raw.ten, false);
        	mSoundIDReady.put(R.raw.come_on, false);
        }
        public SoundPoolPlayer(Context context) {
            mContext = context;

            mSoundIDToPlay = ID_NOT_LOADED;
            mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            mSoundPool.setOnLoadCompleteListener(this);

            initializAllResources();
            Iterator iter = mSoundResMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Integer resId = (Integer)entry.getKey();
				Integer soundLoadId = mSoundPool.load(mContext, resId, 1);
				mSoundResMap.put(resId, soundLoadId);
			}
        }

        @Override
        public synchronized void release() {
            if (mSoundPool != null) {
                mSoundPool.release();
                mSoundPool = null;
            }
        }
        
        private volatile boolean isPlaying = false;
        
        public boolean isPlaying() {
        	return isPlaying;
        }

        @Override
        public synchronized void play(final int action, final int totalMiles, final long totalSeconds, final long nearByOneMileTime) {
            if (action < 0 || action >= mActions.length) {
                Log.e(TAG, "Resource ID not found for action:" + action + " in play().");
                return;
            }
            new Thread() {
				@Override
				public void run() {
					isPlaying = true;
		            if(action == mActions[4]) {
		            	int resourceId;
						try {
							resourceId = mSoundResMap.get(R.raw.dingdong);
							if(resourceId != ID_NOT_LOADED) {
								mSoundPool.play(resourceId, 1f, 1f, 0, 0, 1f);
							}
							Thread.sleep(MILLISECONDS_INTERVAL);
							
							resourceId = mSoundResMap.get(R.raw.you_have_already);
							if(resourceId != ID_NOT_LOADED) {
								mSoundPool.play(resourceId, 1f, 1f, 0, 0, 1f);
							}
							Thread.sleep(MILLISECONDS_INTERVAL);
							
							resourceId = mSoundResMap.get(R.raw.run);
							if(resourceId != ID_NOT_LOADED) {
								mSoundPool.play(resourceId, 1f, 1f, 0, 0, 1f);
							}
							Thread.sleep(MILLISECONDS_INTERVAL);
							
			                playDigitSound(totalMiles);
							Thread.sleep(MILLISECONDS_INTERVAL);
							resourceId = mSoundResMap.get(R.raw.kilometer);
							if(resourceId != ID_NOT_LOADED) {
								mSoundPool.play(resourceId, 1f, 1f, 0, 0, 1f);
							}
							Thread.sleep(MILLISECONDS_INTERVAL);
							resourceId = mSoundResMap.get(R.raw.spend_time);
							if(resourceId != ID_NOT_LOADED) {
								mSoundPool.play(resourceId, 1f, 1f, 0, 0, 1f);
							}
							Thread.sleep(MILLISECONDS_INTERVAL);
							playTimeSound(totalSeconds);
							Thread.sleep(MILLISECONDS_INTERVAL);
							
							resourceId = mSoundResMap.get(R.raw.nearbyonemile);
							if(resourceId != ID_NOT_LOADED) {
								mSoundPool.play(resourceId, 1f, 1f, 0, 0, 1f);
							}
							Thread.sleep(1000);
							
							resourceId = mSoundResMap.get(R.raw.spend_time);
							if(resourceId != ID_NOT_LOADED) {
								mSoundPool.play(resourceId, 1f, 1f, 0, 0, 1f);
							}
							Thread.sleep(MILLISECONDS_INTERVAL);
							playTimeSound(nearByOneMileTime);
							
			            }catch(Exception e) {
			            	e.printStackTrace();
			            }
		            }else {
		                if(action == mActions[0]) {
		                	mSoundIDToPlay = mSoundResMap.get(R.raw.sport_start);
		                }else if(action == mActions[1]) {
		                	mSoundIDToPlay = mSoundResMap.get(R.raw.sport_pause);
		                }else if(action == mActions[2]) {
		                	mSoundIDToPlay = mSoundResMap.get(R.raw.sport_continue);
		                }else if(action == mActions[3]) {
		                	mSoundIDToPlay = mSoundResMap.get(R.raw.sport_completed);
		                }
		                if(mSoundIDToPlay != ID_NOT_LOADED) {
		                    mSoundPool.play(mSoundIDToPlay, 1f, 1f, 0, 0, 1f);
		                }
		            }
		            isPlaying = false;
				}
            	
            }.start();
        }
        

        
        @Override
        public void onLoadComplete(SoundPool pool, int soundID, int status) {
        	Log.d(TAG,"onLoadComplete, status = " + status);
        	Iterator iter = mSoundResMap.entrySet().iterator();
        	if (status != 0) {
        		Log.e(TAG, "loading sound tracks failed (status=" + status + ")");
        		
        		while (iter.hasNext()) {
        		    Map.Entry entry = (Map.Entry) iter.next();
        		    Integer key = (Integer)entry.getKey();
        		    Integer val = (Integer)entry.getValue();
        		    if(val == soundID) {
        		    	mSoundResMap.put(val, ID_NOT_LOADED);
        		    	break;
        		    }
        		}
                return;
            }
            
            if (soundID == mSoundIDToPlay) {
                mSoundIDToPlay = ID_NOT_LOADED;
                Log.d(TAG,"onLoadComplete, play,---");
                mSoundPool.play(soundID, 1f, 1f, 0, 0, 1f);
            }
        }
        
        private void playDigitSound(int digit) throws InterruptedException {
        	int numberOfHundrends = digit / 100;
        	int numberOfTens = (digit - numberOfHundrends * 100) / 10;
        	int numberOfDigits = (digit - numberOfHundrends * 100) % 10;
        	int hundredResoureId;
        	int tenResourceId;
        	int digitResourceId;
        	Log.d(TAG,"numerOfHundreds = " + numberOfHundrends);
        	switch(numberOfHundrends) {
        	case 1:
        		hundredResoureId = mSoundResMap.get(R.raw.one);
        		break;
        	case 2:
        		hundredResoureId = mSoundResMap.get(R.raw.two);
        		break;
        	case 3:
        		hundredResoureId = mSoundResMap.get(R.raw.three);
        		break;
        	case 4:
        		hundredResoureId = mSoundResMap.get(R.raw.four);
        		break;
        	case 5:
        		hundredResoureId = mSoundResMap.get(R.raw.five);
        		break;
        	case 6:
        		hundredResoureId = mSoundResMap.get(R.raw.six);
        		break;
        	case 7:
        		hundredResoureId = mSoundResMap.get(R.raw.seven);
        		break;
        	case 8:
        		hundredResoureId = mSoundResMap.get(R.raw.eight);
        		break;
        	case 9:
        		hundredResoureId = mSoundResMap.get(R.raw.nine);
        		break;
        	default:
        		hundredResoureId = ID_NOT_LOADED;
        		break;
        	}
        	
        	int resId;
        	if(hundredResoureId != ID_NOT_LOADED) {
        		mSoundPool.play(hundredResoureId, 1f, 1f, 0, 0, 1f);
        		Thread.sleep(500);
            	
            	if( (resId = mSoundResMap.get(R.raw.hundred)) != ID_NOT_LOADED) {
            		mSoundPool.play(resId, 1f, 1f, 0, 0, 1f);
            	}
            	Thread.sleep(500);
        	}
        	
        	switch(numberOfTens) {
        	case 1:
        		tenResourceId = mSoundResMap.get(R.raw.one);
        		break;
        	case 2:
        		tenResourceId = mSoundResMap.get(R.raw.two);
        		break;
        	case 3:
        		tenResourceId = mSoundResMap.get(R.raw.three);
        		break;
        	case 4:
        		tenResourceId = mSoundResMap.get(R.raw.four);
        		break;
        	case 5:
        		tenResourceId = mSoundResMap.get(R.raw.five);
        		break;
        	case 6:
        		tenResourceId = mSoundResMap.get(R.raw.six);
        		break;
        	case 7:
        		tenResourceId = mSoundResMap.get(R.raw.seven);
        		break;
        	case 8:
        		tenResourceId = mSoundResMap.get(R.raw.eight);
        		break;
        	case 9:
        		tenResourceId = mSoundResMap.get(R.raw.nine);
        		break;
        	default:
        		tenResourceId = ID_NOT_LOADED;
        	}
        	if(numberOfTens == 0 && numberOfHundrends > 0 && numberOfDigits > 0) {
        		int zeroId = mSoundResMap.get(R.raw.zero);
        		if(zeroId != ID_NOT_LOADED) {
        		    mSoundPool.play(zeroId, 1f, 1f, 0, 0, 1f);
        		}
        	} else if(numberOfTens > 0) {
        		if(tenResourceId != ID_NOT_LOADED) {
            		mSoundPool.play(tenResourceId, 1f, 1f, 0, 0, 1f);
            	}
        		Thread.sleep(500);
            	if( (resId = mSoundResMap.get(R.raw.ten)) != ID_NOT_LOADED) {
            		mSoundPool.play(resId, 1f, 1f, 0, 0, 1f);
            	}
        	}
        	
        	Thread.sleep(500);
        	
        	switch(numberOfDigits) {
        	case 1:
        		digitResourceId = mSoundResMap.get(R.raw.one);
        		break;
        	case 2:
        		digitResourceId = mSoundResMap.get(R.raw.two);
        		break;
        	case 3:
        		digitResourceId = mSoundResMap.get(R.raw.three);
        		break;
        	case 4:
        		digitResourceId = mSoundResMap.get(R.raw.four);
        		break;
        	case 5:
        		digitResourceId = mSoundResMap.get(R.raw.five);
        		break;
        	case 6:
        		digitResourceId = mSoundResMap.get(R.raw.six);
        		break;
        	case 7:
        		digitResourceId = mSoundResMap.get(R.raw.seven);
        		break;
        	case 8:
        		digitResourceId = mSoundResMap.get(R.raw.eight);
        		break;
        	case 9:
        		digitResourceId = mSoundResMap.get(R.raw.nine);
        		break;
        	default:
        		digitResourceId = ID_NOT_LOADED;
        	}
        	
        	if(numberOfTens == 0 && numberOfDigits == 0 && numberOfHundrends == 0) {
        		digitResourceId = mSoundResMap.get(R.raw.zero);
        	}
        	if(digitResourceId != ID_NOT_LOADED) {
        		mSoundPool.play(digitResourceId, 1f, 1f, 0, 0, 1f);
        	}
        }
        
        private void playResSound(int resourceId) {
        	int soundId = mSoundPool.load(mContext, resourceId, 1);
        	if(soundId != ID_NOT_LOADED) {
        		mSoundPool.play(soundId, 1f, 1f, 0, 0, 1f);
        	}
        }
        
		private void playTimeSound(long timeValue) {
			int minutes = (int)timeValue / 60;
			int lastSeconds = (int)timeValue % 60;
			int resouceId;
			try {
				if(minutes > 0) {
				    playDigitSound(minutes);
				    Thread.sleep(MILLISECONDS_INTERVAL);
				    resouceId = mSoundResMap.get(R.raw.minute);
				    if(resouceId != ID_NOT_LOADED) {
					    mSoundPool.play(resouceId, 1f, 1f, 0, 0, 1f);
				    }
				    Thread.sleep(MILLISECONDS_INTERVAL);
				}
				playDigitSound(lastSeconds);
				Thread.sleep(MILLISECONDS_INTERVAL);
				resouceId = mSoundResMap.get(R.raw.second);
				if(resouceId != ID_NOT_LOADED) {
					mSoundPool.play(resouceId, 1f, 1f, 0, 0, 1f);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
        
    }
}
