/* Session Read from file*/
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session {

	public static void main(String[] args) {
		File inFile = null;
		//Map for keeping session details for each user
		Map<String, List<SessionDetails>> sessionList = new HashMap<String, List<SessionDetails>>();
		if (0 < args.length) {
			inFile = new File(args[0]);
		}

		BufferedReader br = null;

		try {

			String sCurrentLine;
			int lineNo = 0;
			long initTime = 0;
			br = new BufferedReader(new FileReader(inFile));
			String lastLine = "";
			while ((sCurrentLine = br.readLine()) != null) {
				lastLine = sCurrentLine;
				if (0 == lineNo) {
					initTime = parseTimeStringToSeconds(sCurrentLine.substring(0, 8));
				}
				lineNo++;

				//For handling session start
				if (sCurrentLine.contains("Start")) {
					String nameStart = sCurrentLine.substring(8, sCurrentLine.indexOf("Start")).trim();
					if (!sessionList.containsKey(nameStart)) { //New user session
						SessionDetails sessionDetails = new SessionDetails();
						if (sessionDetails.getSessionClosed().equals("Y")) {
							sessionDetails.setName(nameStart);
							sessionDetails.setSessionClosed("N");
							sessionDetails.setSessioncount(1);
							sessionDetails.setSessionTime(parseTimeStringToSeconds(sCurrentLine.substring(0, 8)));
							sessionList.put(sessionDetails.getName(), new ArrayList<SessionDetails>());
							sessionList.get(sessionDetails.getName()).add(sessionDetails);
						}
					} else {
						if (null != sessionList.get(nameStart)) { //Existing user session

							SessionDetails sessionDetailsDup = new SessionDetails();
							sessionDetailsDup.setName(nameStart);
							sessionDetailsDup.setSessionClosed("N");
							sessionDetailsDup
									.setTotalTime(sessionDetailsDup.getTotalTime() + sessionDetailsDup.getTotalTime());
							sessionDetailsDup.setSessioncount(1);
							sessionDetailsDup.setSessionTime(parseTimeStringToSeconds(sCurrentLine.substring(0, 8)));
							sessionList.get(sessionDetailsDup.getName()).add(sessionDetailsDup);

						}
					}
				} else if (sCurrentLine.contains("End")) { //For handling session end
					String nameEnd = sCurrentLine.substring(8, sCurrentLine.indexOf("End")).trim();
					if (sessionList.containsKey(nameEnd)) {
						boolean startNotFound = true;
						for (int i = 0; i < sessionList.get(nameEnd).size(); i++) {
							
							SessionDetails sessionDetails = sessionList.get(nameEnd).get(i);
							if (sessionDetails.getSessionClosed().equals("N")) {
								long sessionDur = parseTimeStringToSeconds(sCurrentLine.substring(0, 8))
										- sessionDetails.getSessionTime();
								sessionList.get(sessionDetails.getName()).get(i).setSessionClosed("Y");
								sessionList.get(sessionDetails.getName()).get(i).setSessionTime(0);
								sessionList.get(sessionDetails.getName()).get(i).setTotalTime(sessionDetails.getTotalTime() + sessionDur);
								startNotFound = false;
								break;
								
							}
						}
						if(startNotFound) { //If start not found for end session
							SessionDetails sessionDetails = new SessionDetails();
							long sessionDur = parseTimeStringToSeconds(sCurrentLine.substring(0, 8))
									- initTime;
							sessionDetails.setName(nameEnd);
							sessionDetails.setSessionClosed("Y");
							sessionDetails.setSessioncount(1);
							sessionDetails.setTotalTime(sessionDur);
							sessionList.get(sessionDetails.getName()).add(sessionDetails);

						}
					} else if (!sessionList.containsKey(nameEnd)) {
						SessionDetails sessionDetails = new SessionDetails();
						long sessionDur = parseTimeStringToSeconds(sCurrentLine.substring(0, 8))
								- initTime;
						sessionDetails.setName(nameEnd);
						sessionDetails.setSessionClosed("Y");
						sessionDetails.setSessioncount(1);
						sessionDetails.setTotalTime(sessionDur);
						sessionList.put(sessionDetails.getName(), new ArrayList<SessionDetails>());
						sessionList.get(sessionDetails.getName()).add(sessionDetails);

					}

				}

			}

			for (Map.Entry<String, List<SessionDetails>> entry : sessionList.entrySet()) {

				for (int i = 1; i < entry.getValue().size(); i++) {
					if (entry.getValue().get(i).getSessionClosed().equals("N")) {
						long sessionDur = parseTimeStringToSeconds(lastLine.substring(0, 8))
								- entry.getValue().get(i).getSessionTime();
						entry.getValue().get(i).setTotalTime(entry.getValue().get(i).getTotalTime() + sessionDur);
						entry.getValue().get(i).setSessionClosed("Y");
					}
					if(null != entry.getValue().get(i).getSessionClosed()) { //Calculating total time and count
						entry.getValue().get(0).setSessioncount(
							entry.getValue().get(0).getSessioncount() + entry.getValue().get(i).getSessioncount());

						entry.getValue().get(0).setTotalTime(
							entry.getValue().get(0).getTotalTime() + entry.getValue().get(i).getTotalTime());
					}

				}
				System.out.println(entry.getValue().get(0).getName() + "  " + entry.getValue().get(0).getSessioncount()
						+ "  " + entry.getValue().get(0).getTotalTime());

			}

		}

		catch (

		IOException e) {
			e.printStackTrace();
		}

		finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	// given: mm:ss or hh:mm:ss or hhh:mm:ss, return number of seconds.
	// bad input throws NumberFormatException.
	// bad includes: "", null, :50, 5:-4
	public static long parseTime(String str) throws NumberFormatException {
		if (str == null)
			throw new NumberFormatException("parseTimeString null str");
		if (str.isEmpty())
			throw new NumberFormatException("parseTimeString empty str");

		int h = 0;
		int m, s;
		String units[] = str.split(":");
		assert (units.length == 2 || units.length == 3);
		switch (units.length) {
		case 2:
			// mm:ss
			m = Integer.parseInt(units[0]);
			s = Integer.parseInt(units[1]);
			break;

		case 3:
			// hh:mm:ss
			h = Integer.parseInt(units[0]);
			m = Integer.parseInt(units[1]);
			s = Integer.parseInt(units[2]);
			break;

		default:
			throw new NumberFormatException("parseTimeString failed:" + str);
		}
		if (m < 0 || m > 60 || s < 0 || s > 60 || h < 0)
			throw new NumberFormatException("parseTimeString range error:" + str);
		return h * 3600 + m * 60 + s;
	}

	// given time string (hours:minutes:seconds, or mm:ss, return number of seconds.
	public static long parseTimeStringToSeconds(String str) {
		try {
			return parseTime(str);
		} catch (NumberFormatException nfe) {
			return 0;
		}
	}

	//Class to handle the session object
	public static class SessionDetails {
		String Name = "";
		int sessioncount = 1;
		long sessionTime = 0;
		String sessionClosed = "Y";
		long totalTime = 0;

		public long getTotalTime() {
			return totalTime;
		}

		public void setTotalTime(long totalTime) {
			this.totalTime = totalTime;
		}

		public String getName() {
			return Name;
		}

		public void setName(String name) {
			Name = name;
		}

		public int getSessioncount() {
			return sessioncount;
		}

		public void setSessioncount(int sessioncount) {
			this.sessioncount = sessioncount;
		}

		public long getSessionTime() {
			return sessionTime;
		}

		public void setSessionTime(long sessionTime) {
			this.sessionTime = sessionTime;
		}

		public String getSessionClosed() {
			return sessionClosed;
		}

		public void setSessionClosed(String sessionClosed) {
			this.sessionClosed = sessionClosed;
		}

	}
}