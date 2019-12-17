//UTIL
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
//AWT
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

//SWING
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.GroupLayout;

//SWING.text
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

@SuppressWarnings("serial")
public class OCPTraining extends JFrame implements KeyListener {

    // ===========================
    //  Variabelen en componenten
    // ===========================

    // Swing componenten
    public JTextField txtInput;
    public JLabel lblAntwoord;
    public JLabel lblTitel;
    public JLabel lblScore;
    public JScrollPane scrollOutput;
    public JScrollPane scrollCom;
    public JTextPane txtOutput;
    public JTextPane txtCom;

    // Spelvariabelen
    public String naamSpeler;
    public int bordFormaat;
    public String veldQuatro;
    public int gameStap;
    public String zetSpel;
    public String strOutput;

    // Namen die al een functie hebben in Java (keywords zijn)
    public String keyWords[] = { "abstract", "assert", "boolean",
            "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "extends", "false",
            "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native",
            "new", "null", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch",
            "synchronized", "this", "throw", "throws", "transient", "true",
            "try", "void", "volatile", "while" };

    // ==================
    //  Main componenten
    // ==================

    public static void main(String[] args) {

        /**
         * In de main method wordt Quatro opgestart als GUI.
         */

        // Start Quatro
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new OCPTraining().setVisible(true);
            }
        });

    }

    public OCPTraining()  {

        /**
         * Quatro zet het Window klaar, geeft de openingswoorden weer en zorgt dat de speler input kan geven.
         */

        // Zet de GUI klaar
        maakWindow();

        // Zet het spel klaar

        // Zorg dat de speler met 'enter' het spel verder speelt
        txtInput.addKeyListener(this);

        loadQuestions();

    }

    public void loadQuestions() throws IOException, ParseException {

        File excelFile = new File("recepten.xlsx");
        FileInputStream fis = new FileInputStream(excelFile);

        // we create an XSSF Workbook object for our XLSX Excel File
        XSSFWorkbook workbook = new XSSFWorkbook(fis);

        // we get first sheet
        XSSFSheet sheet = workbook.getSheetAt(0);

        // we iterate on rows
        Iterator<Row> rowIt = sheet.iterator();

        String rcptS[] = new String[4];
        ArrayList<String[]> arrStr = new ArrayList<>();

        // Load excel file
        while (rowIt.hasNext()) {
            Row row = rowIt.next();
            Iterator<Cell> cellIterator = row.cellIterator();

            int iCell = 0;
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                rcptS[iCell] = cell.toString();
                iCell++;
            }
            arrStr.add(rcptS.clone());
        }

        // Full arraylist
        ArrayList<Recept> arrRecepten = new ArrayList<>();
        ArrayList<Ingredient> arrIngredienten = new ArrayList<>();

        // Temp Recept
        Recept tmpR = new Recept();
        ArrayList<Ingredient> lstR = new ArrayList<>();

        boolean frstR = true;
        int itNr = 0;

        for (String[] rcpt : arrStr) {
            if (rcpt[0].equals("<el>")) {
                //tmpR.setIngredientenlijst(lstR);
                arrRecepten.add(tmpR);
                break;
            }
            if (rcpt[0].equals("<nr>")) {
                if (frstR == false) {
                    // Nieuw recept
                    //tmpR.setIngredientenlijst(lstR);
                    arrRecepten.add(tmpR);
                    tmpR = new Recept();
                } else {
                    // Eerste recept
                    tmpR = new Recept();
                    frstR = false;
                }
                itNr = 0;
            } else if (rcpt[0].equals("<rl>")) {
                lstR = new ArrayList<>();
                //lstR.clear();
            } else {
                // Bestaand recept, voeg info toe (1-8 is metainfo, daarna ingredienten)
                itNr++;
                switch (itNr) {
                    case 1:
                        tmpR.setNaam(rcpt[1].trim());
                        break;
                    case 2:
                        tmpR.setSubnaam(rcpt[1].trim());
                        break;
                    case 3:
                        tmpR.setAuteur(rcpt[1].trim());
                        break;
                    case 4:
                        tmpR.setPagina(convertStoI(rcpt[1]));
                        break;
                    case 5:
                        tmpR.setDuur(convertStoI(rcpt[1]));
                        break;
                    case 6:
                        tmpR.setPorties(convertStoI(rcpt[1]));
                        break;
                    case 7:
                        tmpR.setKeuken(rcpt[1].trim());
                        break;
                    case 8:
                        tmpR.setGang(rcpt[1].trim());
                        break;
                    case 9:
                        tmpR.setAllergenen(rcpt[1].trim().toLowerCase());
                        break;
                    case 10:
                        tmpR.setDieten(rcpt[1].trim().toLowerCase());
                        break;
                    default:
                        Ingredient ingNew = new Ingredient();

                        ingNew.setIngrNaam(rcpt[0].trim().toLowerCase());
                        ingNew.setIngrHoeveelheid(Float.parseFloat(rcpt[1]));
                        ingNew.setIngrEenheid(rcpt[2].trim().toLowerCase());
                        ingNew.setRecept(tmpR);

                        arrIngredienten.add(ingNew);
                        break;
                }
            }
        }

        workbook.close();
        fis.close();

        for (Recept rcpt : arrRecepten) {
            receptenRepository.save(rcpt);
        }

        for (Ingredient ing1 : arrIngredienten) {
            ingredientRepository.save(ing1);
        }

        return arrRecepten;
    }


    public void maakWindow() {

        /**
         * In maakWindow wordt de GUI opgezet. Een textveld waarin het programma communiceert,
         * een textveld waarin het veld wordt opgezet en een inputbox waarin de speler antwoord geeft.
         */

        // Creeer GUI-componenten
        txtCom = new JTextPane();
        scrollCom = new JScrollPane(txtCom);

        txtOutput = new JTextPane();
        txtOutput.setFont(new Font("Courier New", Font.PLAIN, 14));
        scrollOutput = new JScrollPane(txtOutput);

        txtInput = new JTextField();

        lblTitel = new JLabel();
        lblTitel.setFont(new Font("Courier New", Font.BOLD, 36));
        lblTitel.setText("OCP Training");

        lblScore = new JLabel();
        lblScore.setFont(new Font("Courier New", Font.PLAIN, 24));
        lblScore.setText("Vragen: || Goed: || Fout: ");

        lblAntwoord = new JLabel();
        lblAntwoord.setText("Antwoord:");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Quatro");

        // Afmetingen txtOutput
        int widthN = 800;
        int heightN = 400;

        // Maak de grouplayout
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        // Maak de horizontale groep
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblTitel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 215, Short.MAX_VALUE)
                                        .addComponent(lblScore))
                                .addComponent(scrollCom)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblAntwoord)
                                        .addComponent(txtInput))
                                .addComponent(scrollOutput, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, widthN, Short.MAX_VALUE))
        );

        // Maak de verticale groep
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblTitel)
                                .addComponent(lblScore))
                        .addComponent(scrollCom, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblAntwoord)
                                .addComponent(txtInput))
                        .addComponent(scrollOutput, GroupLayout.DEFAULT_SIZE, heightN, Short.MAX_VALUE)
        );

        pack();

        // Zet het scherm bovenaan, in het midden van het scherm
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
        int x = (int) ((rect.getMaxX() - getWidth()) / 2);
        setLocation(x, 0);

        // De focus zetten op txtInput (waar de speler de antwoorden typt)
        txtInput.requestFocusInWindow();

    }

    // ============
    //  key Events
    // ============

    public void keyTyped(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            // Bij 'Enter' gaat het spel verder met de nieuwe invoer
            playGame(txtInput.getText());
        }
    }

    // ==================
    //  Game componenten
    // ==================

    public void playGame(String strInput) {

        /**
         * playGame verzorgt het verloop van het spel. Eerst worden de benodigde gegevens
         * verzameld (naam, grootte van het bord) en daarna wordt het bord opgezet. Daarna wordt
         * om de beurt de speler en het spel aan zet gelaten. Tenslotte wordt het spel afgesloten
         * als er geen zetten meer mogelijk zijn en - zo gewenst - het spel opnieuw gestart of het
         * spel afgesloten.
         */

        strOutput = "";

        // Geef de gegeven input weer
        outputNaarPane(txtCom, strInput + "\n", Color.BLUE);

        // Kies het juiste spelonderdeel op basis van de variabele gameStap
        switch (gameStap) {

            // Opslaan naam van de speler
            case 0:

                break;

            // Opslaan grootte van het bord
            case 1:

                break;


        }

        // Weergeven output en resetten txtInput
        outputNaarPane(txtCom, strOutput, Color.RED);
        outputNaarPane(txtCom, "\n > ", Color.BLUE);
        txtInput.setText("");
        txtInput.requestFocusInWindow();

    }

    // ====================
    //  Output componenten
    // ====================

    public void outputNaarPane(JTextPane tp, String msg, Color c) {

        /**
         * outputNaarPane schrijft de String 'msg' naar het einde van JTextPane 'tp'. Dit gebeurt in kleur 'c'.
         */

        // Stel de kleur in
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet kleur = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        // Voeg msg toe aan het eind van tp, in kleur c
        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(kleur, false);
        tp.replaceSelection(msg);

    }

}
