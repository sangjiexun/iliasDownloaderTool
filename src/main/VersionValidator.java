package main;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;

import model.StorageProvider;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import view.LookAndFeelChanger;

public class VersionValidator {
	private final int YES = 0;
	private StorageProvider storageProvider;
	private String numberOfCommits = "-1";

	public boolean validate() {
		LookAndFeelChanger.changeToNative();
		storageProvider = new StorageProvider();

		if (storageProvider.firstStart()) {
			storageProvider.storeFirstStart(false);
			return false;
		}
		if (!newVersionAvailable()) {
			return false;
		}
		int answer = JOptionPane.showConfirmDialog(null, "   download new version?", "new version available!", JOptionPane.YES_NO_OPTION,
				JOptionPane.INFORMATION_MESSAGE);
		if (answer == YES) {
			storageProvider.storeFirstStart(true);
			storageProvider.storeCommitVersion(numberOfCommits);
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(
							new URI("https://github.com/DeOldSax/iliasDownloaderTool/raw/master/IliasDownloaderTool.jar"));
				} catch (IOException | URISyntaxException e) {
					e.printStackTrace();
				}
			} else {
				// TODO better option to copy link
				JOptionPane.showMessageDialog(null, "https://github.com/DeOldSax/iliasDownloaderTool/raw/master/IliasDownloaderTool.jar",
						"follow link", JOptionPane.INFORMATION_MESSAGE);
			}
			return true;
		}
		return false;
	}

	private boolean newVersionAvailable() {
		DefaultHttpClient client = new DefaultHttpClient();
		final HttpGet request = new HttpGet("https://github.com/DeOldSax/iliasDownloaderTool");
		HttpResponse response = null;
		String content = null;
		try {
			response = client.execute(request);
			HttpEntity entity = response.getEntity();
			content = EntityUtils.toString(entity);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		request.releaseConnection();

		final Document doc = Jsoup.parse(content);
		final Elements select = doc.select("span");
		for (Element element : select) {
			if (element.text().matches("\\d+") && element.toString().contains("octicon-history")) {
				numberOfCommits = element.text();
				final int lastCommitVersion = Integer.parseInt(storageProvider.getCommitVersion());
				final int newCommitVersion = Integer.parseInt(numberOfCommits);
				if (lastCommitVersion != newCommitVersion && !storageProvider.firstStart()) {
					return true;
				}
			}
		}
		return false;
	}
}