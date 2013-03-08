package com.example.ezreader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.iflytek.speech.SpeechError;
import com.iflytek.speech.SynthesizerPlayer;
import com.iflytek.speech.SynthesizerPlayerListener;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

@SuppressLint("SdCardPath")
public class MainActivity extends Activity implements SynthesizerPlayerListener {


	private HeadsetPlugReceiver headsetPlugReceiver;
	
	//�ı���ʾ����
	TextView mTV;

	//��ʾ�ļ�����
	TextView mTV_total;

	//��ʾ�ļ�����
	EditText mET_pos;
	
	//�ı��ַ���
	String mCharset;

	//�ļ�����
	File mFile = null;

	//��������ļ�����
	RandomAccessFile mRandomAccessFile = null;

	//��ǰλ��
	private boolean mFlagEnd = false;
	
	//��ǰλ��
	private int mCurReadPos = 0;

	//ӳ�仺�����
	private  MappedByteBuffer mMapBuffer = null;

	//�ı������С
	private static final int TEXT_BUFFER_SIZE = 1024 * 1;
	
	//�ı���������
	private static byte[] mBytes = new byte[TEXT_BUFFER_SIZE];
	
	//�ļ�·��
	public static final String SDCARD_FILEDIR_PATH = "/sdcard/Download/";
	
	//�ļ���
	public static final String DEFAULT_FILE_NAME = "����.txt";
	
	//�������.
	private SharedPreferences mSharedPreferences;
	
	//�ϳɶ���.
	private SynthesizerPlayer mSynthesizerPlayer;

	//�������
	private int mPercentForBuffering = 0;
	
	//���Ž���
	private int mPercentForPlaying = 0;

	private long mFileTotalSize = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        /* register receiver */
        registerHeadsetPlugReceiver();
        
		// ���SDCard�Ƿ����
		if(SDCardUtil.checkSDCARD()){

			mTV = (TextView) findViewById(R.id.txt_result);
			mTV_total = (TextView) findViewById(R.id.tv_total);

	        // ����Ŀ¼�����к�׺��Ϊtxt���ļ���
	        File[] files = SDCardUtil.findSDCardFile(SDCARD_FILEDIR_PATH, "txt");
	        if(files != null && files.length > 0){
	            for(int i=0; i<files.length; i++){
	                if(DEFAULT_FILE_NAME.equals(files[i].getName())){
	                	mFile = files[i];
	                }
	            }
	        }
		    if(mFile == null){
		    	mFile = files[0];
		    }
		    mCharset = get_charset(mFile);
			mSharedPreferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);

	        try {
	            mRandomAccessFile = new RandomAccessFile(mFile, "r");
	            mFileTotalSize = mRandomAccessFile.length();
		        mMapBuffer = mRandomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, mFileTotalSize);
	        } catch (Exception e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
		}
		Button bt_go = (Button)findViewById(R.id.bt_go);
		bt_go.setOnClickListener(new Button.OnClickListener() {
	         
	        @Override
	        public void onClick(View arg0) {
	        	goText();
	        }
	    });
		bt_go.requestFocus();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void goText() {
		mET_pos = (EditText) findViewById(R.id.et_pos);
		mCurReadPos = Integer.parseInt(mET_pos.getText().toString());
		if (mCurReadPos <=  mFileTotalSize) {
			mMapBuffer.position(mCurReadPos);
			mFlagEnd = true;
			readFileToText();
			synthetizeInSilence();
		}
	}
	
	private int readFileToText() {
		mTV_total.setText(Integer.toString(mCurReadPos) + "/" + Long.toString(mFileTotalSize));
    	int size = getFixedReadMaxNum();
    	if (size == 0){
    		return size;
    	}
    	int posn = 0;
    	int totalread = 0;
		String content = "";
        try {
        	while (posn == 0){
				mMapBuffer.get(mBytes, 0, size);
            	if (size < TEXT_BUFFER_SIZE) {
            		posn = size;
            		mCurReadPos += size;
	        		totalread += size;
					content += new String(mBytes, 0, posn, mCharset);
            	}
            	else {
					int i = size-1;
					for(; i>=0; i--) {
						if(mBytes[i] == '\n'){
							posn=i+1;
					        break;
						}
					}
			        if (posn != 0) {
			        	mCurReadPos += posn;
		        		totalread += posn;
						content += new String(mBytes, 0, posn, mCharset);
			        }
			        else {
			        	mCurReadPos += size;
		        		totalread += size;
						content += new String(mBytes, mCharset);
			        }
            	}
				mMapBuffer.position(mCurReadPos);
        	}
    		mTV.setText(content);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        
		return totalread;
	}

	/**
	 * ʹ��SynthesizerPlayer�ϳ��������������ϳ�Dialog.
	 * @param
	 */
	private void synthetizeInSilence() {
		if (null == mSynthesizerPlayer) {
			//�����ϳɶ���.
			mSynthesizerPlayer = SynthesizerPlayer.createSynthesizerPlayer(
					this, "appid=" + getString(R.string.app_id));
		}
		else {
			mSynthesizerPlayer.cancel();
		}

		//���úϳɷ�����.
		String role = mSharedPreferences.getString(
				getString(R.string.preference_key_tts_role),
				getString(R.string.preference_default_tts_role));
		mSynthesizerPlayer.setVoiceName(role);

		//���÷���������
		int speed = mSharedPreferences.getInt(
				getString(R.string.preference_key_tts_speed),
				100);
		mSynthesizerPlayer.setSpeed(speed);

		//��������.
		int volume = mSharedPreferences.getInt(
				getString(R.string.preference_key_tts_volume),
				100);
		mSynthesizerPlayer.setVolume(volume);

		//���ñ�����.
		String music = mSharedPreferences.getString(
				getString(R.string.preference_key_tts_music),
				getString(R.string.preference_default_tts_music));
		if (music != "") mSynthesizerPlayer.setBackgroundSound(music);

		//��ȡ�ϳ��ı�.
		String source = null;
		source = String
				.format((String)mTV.getText(), 0, 0);
		
		//���������ϳ�.
		mSynthesizerPlayer.playText(source, null,this);
	}

	/**
	 * SynthesizerPlayerListener��"���Ž���"�ص��ӿ�.
	 * @param percent,beginPos,endPos
	 */
	@Override
	public void onBufferPercent(int percent,int beginPos,int endPos) {
		mPercentForBuffering = percent;
	}

	/**
	 * SynthesizerPlayerListener��"���Ž���"�ص��ӿ�.
	 * @param percent,beginPos,endPos
	 */
	@Override
	public void onPlayPercent(int percent,int beginPos,int endPos) {
		mPercentForPlaying = percent;
		mFlagEnd = false;
	}

	/**
	 * SynthesizerPlayerListener��"��ʼ����"�ص��ӿ�.
	 * @param 
	 */
	@Override
	public void onPlayBegin() {
	}

	/**
	 * SynthesizerPlayerListener��"��ͣ����"�ص��ӿ�.
	 * @param 
	 */
	@Override
	public void onPlayPaused() {
	}

	/**
	 * SynthesizerPlayerListener��"�ָ�����"�ص��ӿڣ���ӦonPlayPaused
	 * @param 
	 */
	@Override
	public void onPlayResumed() {
	}

	/**
	 * SynthesizerPlayerListener��"�����Ự"�ص��ӿ�.
	 * @param error
	 */
	@Override
	public void onEnd(SpeechError error) {
		if(checkEnd()){
			mFlagEnd = false;
			return;
		}
		if (readFileToText() == 0){
			return;
		}
		//��ȡ�ϳ��ı�.
		String source = null;
		source = String
				.format((String)mTV.getText(), 0, 0);
		
		//���������ϳ�.
		mSynthesizerPlayer.playText(source, null,this);
	}
	
	public static String get_charset( File file ) {
	    String charset = "GBK";
	    byte[] first3Bytes = new
	    byte[3];
	    try {
	        boolean checked =false;
	        BufferedInputStream bis = new BufferedInputStream( new FileInputStream( file ) );
	        bis.mark( 0 );
	        int read = bis.read( first3Bytes, 0, 3 );
	        if ( read == -1 ) return charset;
	        if ( first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE ) {
	            charset = "UTF-16LE";
	            checked = true;
	        }
	        else
	        if ( first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF ) {
	            charset = "UTF-16BE";
	            checked = true;
	        }
	        else
	        if ( first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF ) {
	            charset = "UTF-8";
	            checked = true;
	        }
	        bis.reset();
	        if ( !checked ) {
	            //    int len = 0;
	            int loc = 0;
	            while ( (read = bis.read()) != -1 ) {
	                loc++;
	                if ( read >= 0xF0 ) break;
	                if ( 0x80 <= read && read <= 0xBF ) // ��������BF���µģ�Ҳ����GBK
	                break;
	                if ( 0xC0 <= read && read <= 0xDF ) {
	                    read = bis.read();
	                    if ( 0x80 <= read && read <= 0xBF ) // ˫�ֽ� (0xC0 - 0xDF) (0x80
	                    // - 0xBF),Ҳ������GB������
	                    continue;
	                    else
	                    break;
	                }
	                else
	                if ( 0xE0 <= read && read <= 0xEF ) {// Ҳ�п��ܳ������Ǽ��ʽ�С
	                    read = bis.read();
	                    if ( 0x80 <= read && read <= 0xBF ) {
	                        read = bis.read();
	                        if ( 0x80 <= read && read <= 0xBF ) {
	                            charset = "UTF-8";
	                            break;
	                        }
	                        else
	                        break;
	                    }
	                    else
	                    break;
	                }
	            }
	            //System.out.println( loc + " " + Integer.toHexString( read ) );
	        }
	        bis.close();
	    } catch ( Exception e ) {
	        e.printStackTrace();
	    }
	    return charset;
	}

	private int getFixedReadMaxNum()
	{
		return mMapBuffer.remaining() > TEXT_BUFFER_SIZE ? TEXT_BUFFER_SIZE : mMapBuffer.remaining();
	}

	private boolean checkEnd()
	{
		return mFlagEnd;
	}
	
	private void registerHeadsetPlugReceiver() {
		headsetPlugReceiver = new HeadsetPlugReceiver(); 
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver(headsetPlugReceiver, intentFilter);
	}
    
    @Override
    public void onDestroy() {
    	unregisterReceiver(headsetPlugReceiver);
    	super.onDestroy();
    }     
	
}
