package raptor.dictionary.main;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class Main extends JFrame {

	public Main() throws IOException {
		final Dictionary dict = new Dictionary();

		final File source = new File("dictionary.txt");
		final File target = new File("new-dictionary.txt");

		dict.readDictFile(source);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setBounds(0, 0, 1000, 600);

		final JPanel container = new JPanel();
		container.setVisible(true);

		final JTextArea output = new JTextArea();
		output.setPreferredSize(new Dimension(800, 500));
		output.setEditable(false);
		output.setVisible(true);
		output.setLineWrap(true);

		final JTextField chatInput = new JTextField();
		chatInput.setPreferredSize(new Dimension(400, 25));
		chatInput.setVisible(true);

		final JTextField partOfSpeech = new JTextField();
		partOfSpeech.setPreferredSize(new Dimension(400, 25));
		partOfSpeech.setVisible(true);

		final JButton submit = new JButton("Submit");
		submit.setPreferredSize(new Dimension(100, 25));
		submit.setVisible(true);

		final Action submitAction = new AbstractAction("submit") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				final String word = chatInput.getText();
				final String pos = partOfSpeech.getText();
				chatInput.setText("");
				if (word == null || word.trim().equals(""))
					return;
				final boolean successful = dict.addWord(pos, word);
				if (successful) {
					output.insert(word + " was successfully added as a " + pos + "\n", 0);
				} else {
					output.insert(word + " already exists!\n", 0);
				}
			}
		};
		submit.setAction(submitAction);
		submitAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_ENTER);
		submit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit");
		submit.getActionMap().put("submit", submitAction);

		final JButton write = new JButton("Write To File");
		write.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					dict.writeDictFile(target);
				} catch (IOException e1) {
					throw new RuntimeException();
				}
			}
		});
		submit.setPreferredSize(new Dimension(100, 25));
		submit.setVisible(true);

		container.add(output);
		container.add(chatInput);
		container.add(partOfSpeech);
		container.add(submit);
		container.add(write);

		this.add(container);
		setVisible(true);
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		new Main();
	}

	private static class Dictionary {
		private final Map<String, List<String>> wordsByPos;
		private String foreword;

		public Dictionary() throws IOException {
			wordsByPos = new HashMap<String, List<String>>();
			foreword = "";
		}

		public boolean addWord(final String p, final String word) {
			if (wordsByPos.get(p) == null)
				wordsByPos.put(p, new ArrayList<String>());
			else if (wordsByPos.get(p).contains(word))
				return false;

			wordsByPos.get(p).add(word);

			return true;
		}

		public void writeDictFile(final File dictFile) throws IOException {
			final StringBuffer buffer = new StringBuffer();
			buffer.append(foreword);

			for (final Map.Entry<String, List<String>> l : wordsByPos.entrySet())
				l.getValue().sort(new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						return o1.compareTo(o2);
					}
				});

			for (final Map.Entry<String, List<String>> e : wordsByPos.entrySet()) {
				for (final String w : e.getValue())
					buffer.append(w + "\t" + e.getKey() + "\n");
				buffer.append("\n");
			}

			final FileWriter writer = new FileWriter(dictFile, false);
			writer.write(buffer.toString());
			writer.close();
		}

		public void readDictFile(final File dictFile) throws IOException {
			for (final Map.Entry<String, List<String>> l : wordsByPos.entrySet())
				l.getValue().clear();

			final BufferedReader reader = new BufferedReader(new FileReader(dictFile));

			String line = "";
			// Read the foreword
			foreword = "";
			while (reader.ready()) {
				line = reader.readLine();
				if (line.startsWith("#"))
					foreword += line + "\n";
				else
					break;
			}
			// Read the dictionary (comments are ignored entirely)
			while (reader.ready()) {
				if (line == null || line.startsWith("#") || line.trim().equals("")) {
					line = reader.readLine();
					continue;
				}

				final String[] split = line.split("\t");
				final String partOfSpeech = split[1];
				final String word = split[0];

				if (wordsByPos.get(partOfSpeech) == null)
					wordsByPos.put(partOfSpeech, new ArrayList<String>());

				final List<String> wordList = wordsByPos.get(partOfSpeech);
				if (!wordList.contains(word))
					wordList.add(word);

				line = reader.readLine();
			}

			reader.close();

			for (final Map.Entry<String, List<String>> l : wordsByPos.entrySet())
				l.getValue().sort(new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						return o1.compareTo(o2);
					}
				});
		}
	}
}
