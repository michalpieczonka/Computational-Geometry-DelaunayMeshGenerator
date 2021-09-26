
package GOPR_2;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Michal
 */

public class MeshPanel extends JPanel {
    
    MeshParameters config = new MeshParameters(); //Konfiguracja glowna danych siatki odpowiada za liczbe punktow na ktorych bazuje delaunay oraz za zageszczenie/dokladnosc siatki
    public static long meshGeneratingTime; //Licznik czasu -> gadzet do mozliwosci analizy wplywu gestosci i liczby punktow na czas generowania siatki
    private BufferedImage image; //Poczatkowy obraz wejsciowy na ktorym beda przeprowadzane wszystkie operacje zwiazane z generowaniem siatki -> pelni role bufora "resetujacego" gdyby chcialo sie 
                                // "kombinowac" z parametrami generowanej siatki
    private BufferedImage meshBuffer; //Bufor pelniacy role tymczasowego, na ktorym bedzie tworzona siatka -> tak naprawde jest buforem wyjsciowym i najwazniejszym, przechowujacym wszystkie dane
    public static List<Point> pointsOnMesh;
    
    public MeshPanel() {
       try {                
          image = ImageIO.read(new File("singleSlice.png"));   //Wczytanie glownego obrazka zawierajacego strukture     
          meshBuffer = image; //Przypisanie w pierwszym uruchomieniu glownego obrazka zawierajacego strukture do buforu, ktory jest rysowany i na ktorym generowana jest siatka
       } catch (IOException ex) {
            System.out.println("Wystapil problem z wczytaniem pliku graficznego do generowania siatki");
       }
    }

    //Glowna metoda rysujaca obraz
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
            g.drawImage(meshBuffer, 0, 0, this); // Narysowanie "na panelu" obrazka, w zaleznosci od jego stanu -> poczatkowo bedzie to domyslny obraz a pozniej za kazdym razem nowa siatka
    }
    
    
    //Generowanie siatki na mikrostrukturze bazujac na ustawieniach wybranych przez uzytkownika
    public void generateUserSettingsMesh(int pointCount, int accuracy, boolean fill) throws IOException{
         //Przypisanie
        config.pointCount = pointCount;
        config.accuracy = accuracy;
        config.fill = fill;
        
        meshBuffer.getGraphics().dispose();
        
        long startTime = System.currentTimeMillis();
        meshBuffer = MeshGenerator.generateMesh(image, config); //Wygenerowanie siatki
        long endTime = System.currentTimeMillis();
        meshGeneratingTime = endTime - startTime;
    }

    
    //Generowanie siatki na mikrostrukturze bazujac na moim zdaniem najlepszych parametrach
    public void generateBestMesh() throws FileNotFoundException, IOException {
        //Najlepsze wartosci na podstawie testow
        int pointCount = 400;  //400 punktow
        int accuracy = 20; //Zageszczenie/dokladnosc 20
        boolean fill = true; //Wyplenienie bazowym kolorem kazdego ziarna
        //Przypisanie
        config.accuracy = accuracy;
        config.pointCount = pointCount;
        config.fill = fill;
        
        meshBuffer.getGraphics().dispose(); //Wyczyszczenie bufora z aktualna siatka
        
        long startTime = System.currentTimeMillis();
        
        meshBuffer = MeshGenerator.generateMesh(image, config); //Wygenerowanie siatki
        
        long endTime = System.currentTimeMillis();
        meshGeneratingTime = endTime - startTime;
       List<Grain> grainsList = MeshGenerator.identifyAllGrains(pointsOnMesh, image);
       fillGrainsListinGui(grainsList);
       System.out.println(grainsList.size());
    }
    
    //Wypelnienie listy znajdujacej sie w GUI zidentyfikowanymi ziarnami ustawiajac na tlo kazdej pozycji, kolor ziarna
    void fillGrainsListinGui(List<Grain> grainsList){
        DefaultListModel listModel = new DefaultListModel();
        for (int i=0; i< grainsList.size(); i++){
            listModel.addElement(grainsList.get(i));
        }
        GUI.guiGrainList.setModel(listModel);
        GUI.guiGrainList.setCellRenderer(new DefaultListCellRenderer() {

            //Nadanie kazdemu rekordowi na liscie w GUI koloru na podstawie pobranych wartosci RGB
                     @Override
                     public Component getListCellRendererComponent(JList list, Object value, int index,
                               boolean isSelected, boolean cellHasFocus) {
                          Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                          if (value instanceof Grain) {
                               Grain nextGrain = (Grain) value;
                               setText(nextGrain.toString());
                               Color grainColor = new Color (nextGrain.red, nextGrain.green, nextGrain.blue);
                               setBackground(grainColor);
                               if (isSelected) {
                                    setBackground(getBackground().darker());
                               }
                          } 
                          return c;
                     }

                });
    }
    
    //Zapisywanie siatki do pliku png
    public void saveMeshToPng() throws FileNotFoundException, IOException{       
        String fileName;
        fileName= JOptionPane.showInputDialog ("Wprowadz nazwe pliku do zapisu siatki");
        FileOutputStream outputStream = new FileOutputStream(new File(fileName+".png"));  
        ImageIO.write(meshBuffer, "png", outputStream);
        JOptionPane.showMessageDialog(null, "Siatka zostala zapisana\nSprawdz folder", "Sukces", JOptionPane.INFORMATION_MESSAGE);
        outputStream.close();
    }
    
    
    //Zapisywanie siatki - konkretniej wszystkich punktow do pliku txt
    public void saveMeshToTxt() throws IOException{
        String fileName;
        fileName= JOptionPane.showInputDialog ("Wprowadz nazwe pliku do zapisu siatki");
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName+".txt"));  
         
        bw.write(GUI.lTrianglesNumber.getText()+"\n"+GUI.lPointsNumber.getText()+"\n"+GUI.lTimeGenerated.getText()+"\nWspolrzedne wszystkich wygenerowanych punktow: \n");
        int pointsCounter = 1;
        for (Point p: pointsOnMesh){
             bw.write(pointsCounter+" | (" + p.x + ";" + p.y + ")\n");
             pointsCounter++;
        }
 
        bw.close();
    }
    
    
    //Zapisywanie siatki - konkretniej wszystkich punktow do wizualizatora - testowane w meshLab
    public void saveMeshToVisualizer() throws IOException{
        String fileName;
        fileName= JOptionPane.showInputDialog ("Wprowadz nazwe pliku do zapisu siatki");
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName+".txt"));
        for (Point p: pointsOnMesh){
             bw.write("" + p.x + ";" + p.y +";" + 0 + "\n");
        }
        bw.close();
    }
    
    
    
}
