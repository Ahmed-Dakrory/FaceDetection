import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileNameExtensionFilter;




import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.sarxos.webcam.Webcam;
public class FaceTest {
	
	static String  destinationFolder="";
	static JFrame frame;
	static JButton selectFileButton;
	static JButton selectDistinationButton;
	static JLabel distinationLabel;
	static JProgressBar progressBar;
	static int progressValue=0;
	public static void main(String[] args) throws Exception{
		
		
		progressBar = new JProgressBar(0,100);
		progressBar.setValue(100);
		progressBar.setStringPainted(true);
		progressBar.setBounds(50, 250, 200, 30);
		
		frame = new JFrame("Face Recognition");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLocation(500, 150);
        selectFileButton=new JButton("Select File");
        selectFileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub

				if(!destinationFolder.equals("")){
				OpenFileChooser();
				}
			}
		});
        selectFileButton.setBounds(50, 200, 200, 30);
        
        selectDistinationButton=new JButton("Select Distination for Images");
        selectDistinationButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
					OpenFolderChooser();
				
			}
		});
        selectDistinationButton.setBounds(50, 100, 200, 30);
        
        distinationLabel=new JLabel("Please select the distination for face you want to save");
        distinationLabel.setBounds(50, 150, 500, 30);
        
        frame.add(selectFileButton);
        frame.add(selectDistinationButton);
        frame.add(distinationLabel);
        frame.add(progressBar);
        frame.setLayout(null);
        frame.setVisible(true);;
		
		
	}
	private static void OpenFileChooser(){
		//Create a file chooser
	            progressBar.setValue(0);
	            	
	         // get default webcam and open it
				Webcam webcam = Webcam.getDefault();
				webcam.open();

				// get image
				BufferedImage image = webcam.getImage();

				// save image to PNG file
				try {
					ImageIO.write(image, "jpg", new File(destinationFolder+"\\"+"image.jpg"));
		        	webcam.close();
		            new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub

				            makeThePhotoAnalysis(destinationFolder+"\\"+"image.jpg");
						}
					}).start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
	        

	}
	
	private static void OpenFolderChooser(){
		//Create a file chooser
		 JFileChooser chooser = new JFileChooser("C:\\");
		 chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	        FileNameExtensionFilter filter = new FileNameExtensionFilter(
	                "JPG & GIF Images", "jpg", "gif");
	        chooser.setFileFilter(filter);
	        int returnVal = chooser.showOpenDialog(null);
	        if(returnVal == JFileChooser.APPROVE_OPTION) {
	            System.out.println("You chose to open this file: " +
	                    chooser.getSelectedFile().getAbsolutePath());
	            destinationFolder=chooser.getSelectedFile().getAbsolutePath();
	            distinationLabel.setText(destinationFolder);
	        }

	}
	
	private static void makeThePhotoAnalysis(String fileName) {
		// TODO Auto-generated method stub
		File file = new File(fileName);
		byte[] buff = getBytesFromFile(file);
		
		String url = "https://api-us.faceplusplus.com/facepp/v3/detect";
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, byte[]> byteMap = new HashMap<>();
        map.put("api_key", "P_agpqBUHnhJLKd4LrLqtMFY5Pmqkyzx");
        map.put("api_secret", "Tp5KhMBSgy6HU07x4nu5wKR_MmdHzTrp");
        byteMap.put("image_file", buff);
        try{
        	if(progressValue<30){
    			progressValue=20;
    	        progressBar.setValue(progressValue);
    			}
            byte[] bacd = post(url, map, byteMap);
            String str = new String(bacd);
            showTheImageWithFaces(str,buff);
            progressBar.setValue(100);
        }catch (Exception e) {
        	e.printStackTrace();
        	progressBar.setValue(100);
		}
	}


	private static void showTheImageWithFaces(String str,byte[] buff) {
		// TODO Auto-generated method stub
		try {
			JSONObject faces=new JSONObject(str);
			JSONArray faceArray = faces.getJSONArray("faces");
			Rectangle facesRec[]=new Rectangle[faceArray.length()];
			System.out.println(faceArray.length());
			for(int i=0;i<faceArray.length();i++){
				JSONObject faceObject=new JSONObject(faceArray.get(i).toString());
				
				JSONObject details=new JSONObject(faceObject.getJSONObject("face_rectangle").toString());
				
				
				Rectangle face=new Rectangle(details.getInt("left"),
											 details.getInt("top"),
											 details.getInt("width"),
											 details.getInt("height"));
				facesRec[i]=face;
				
			}
			showImage(buff,facesRec);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private final static int CONNECT_TIME_OUT = 30000;
    private final static int READ_OUT_TIME = 50000;
    private static String boundaryString = getBoundary();
    @SuppressWarnings("unchecked")
	protected static byte[] post(String url, HashMap<String, String> map, HashMap<String, byte[]> fileMap) throws Exception {
        HttpURLConnection conne;
        URL url1 = new URL(url);
        conne = (HttpURLConnection) url1.openConnection();
        conne.setDoOutput(true);
        conne.setUseCaches(false);
        conne.setRequestMethod("POST");
        conne.setConnectTimeout(CONNECT_TIME_OUT);
        conne.setReadTimeout(READ_OUT_TIME);
        conne.setRequestProperty("accept", "*/*");
        conne.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);
        conne.setRequestProperty("connection", "Keep-Alive");
        conne.setRequestProperty("user-agent", "Mozilla/4.0 (compatible;MSIE 6.0;Windows NT 5.1;SV1)");
        progressValue=30;
        progressBar.setValue(progressValue);
        
        DataOutputStream obos = new DataOutputStream(conne.getOutputStream());
        Iterator<?> iter = map.entrySet().iterator();
        while(iter.hasNext()){
			@SuppressWarnings("rawtypes")
			
			Map.Entry<String, String> entry = (Map.Entry) iter.next();
            String key = entry.getKey();
            String value = entry.getValue();
            obos.writeBytes("--" + boundaryString + "\r\n");
            obos.writeBytes("Content-Disposition: form-data; name=\"" + key
                    + "\"\r\n");
            obos.writeBytes("\r\n");
            obos.writeBytes(value + "\r\n");
            if(progressValue<70){
    			progressValue=progressValue+10;
    	        progressBar.setValue(progressValue);
    			}
        }
        if(fileMap != null && fileMap.size() > 0){
            Iterator<?> fileIter = fileMap.entrySet().iterator();
            while(fileIter.hasNext()){
                Map.Entry<String, byte[]> fileEntry = (Map.Entry<String, byte[]>) fileIter.next();
                obos.writeBytes("--" + boundaryString + "\r\n");
                obos.writeBytes("Content-Disposition: form-data; name=\"" + fileEntry.getKey()
                        + "\"; filename=\"" + encode(" ") + "\"\r\n");
                obos.writeBytes("\r\n");
                obos.write(fileEntry.getValue());
                obos.writeBytes("\r\n");
                if(progressValue<80){
                	progressValue=progressValue+10;
        	        progressBar.setValue(progressValue);
        			}
            }
        }
        obos.writeBytes("--" + boundaryString + "--" + "\r\n");
        obos.writeBytes("\r\n");
        obos.flush();
        obos.close();
        InputStream ins = null;
        int code = conne.getResponseCode();
        try{
            if(code == 200){
                ins = conne.getInputStream();
            }else{
                ins = conne.getErrorStream();
            }
        }catch (SSLException e){
            e.printStackTrace();
            progressBar.setValue(100);
            return new byte[0];
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[4096];
        int len;
        while((len = ins.read(buff)) != -1){
            baos.write(buff, 0, len);
            if(progressValue<90){
            	progressValue=progressValue+10;
    	        progressBar.setValue(progressValue);
    			}
        }
        byte[] bytes = baos.toByteArray();
        ins.close();
        return bytes;
    }
    private static String getBoundary() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < 32; ++i) {
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-".charAt(random.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_".length())));
        }
        return sb.toString();
    }
    private static String encode(String value) throws Exception{
        return URLEncoder.encode(value, "UTF-8");
    }
    
    public static byte[] getBytesFromFile(File f) {
        if (f == null) {
            return null;
        }
        try {
            FileInputStream stream = new FileInputStream(f);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = stream.read(b)) != -1)
                out.write(b, 0, n);
            stream.close();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
        }
        return null;
    }

    public static void showImage(byte[] image,Rectangle[] recArray){
    	JFrame frame1 = new JFrame("image");
        frame1.setLayout(new BorderLayout());
        frame1.add(new ImagePanel(image,recArray));
        frame1.pack();
        frame1.setLocationRelativeTo(null);
        frame1.setVisible(true);
        if(!destinationFolder.equals("")){
        showAllImagesOfFaces(image,recArray);
        }
    }
    
    public static void showAllImagesOfFaces(byte[] image,Rectangle[] recArray){
    	
		try {
			BufferedImage imageR = ImageIO.read(new ByteArrayInputStream(image));
			for(int i=0;i<recArray.length;i++){
	        	BufferedImage out = imageR.getSubimage(recArray[i].x,
	        			recArray[i].y,
	        			recArray[i].width,
	        			recArray[i].height);
	        	ImageIO.write(out, "jpg", new File(destinationFolder+"\\"+String.valueOf(i)+".jpg"));
	        	
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
    
    public static class ImagePanel extends JPanel {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private BufferedImage floorPlan;

        private Rectangle faces[] ;

        public ImagePanel(byte[] image,Rectangle[] recArray) {
            try {
                floorPlan = ImageIO.read(new ByteArrayInputStream(image));
                
                this.faces=recArray;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return floorPlan == null ? new Dimension(200, 200) : new Dimension(floorPlan.getWidth(), floorPlan.getHeight());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            if (floorPlan != null) {

                int x = (getWidth() - floorPlan.getWidth()) / 2;
                int y = (getHeight() - floorPlan.getHeight()) / 2;
                g2d.drawImage(floorPlan, x, y, this);

                g2d.setColor(Color.RED);
                g2d.translate(x, y);
                if(faces!=null){
	                for(int i=0;i<faces.length;i++){
	                	g2d.draw(faces[i]);
	                }
                }
            }

            g2d.dispose();
        }
    }

}