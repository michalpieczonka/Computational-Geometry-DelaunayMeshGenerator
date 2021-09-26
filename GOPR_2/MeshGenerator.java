package GOPR_2;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michal
 */
public class MeshGenerator {

    
        public static BufferedImage generateMesh(BufferedImage image,MeshParameters config) throws IOException {
        
        int pointCount=config.pointCount; //Liczba punktow na bazie ktorych zostanie utworzona siatka
        int accuracy=config.accuracy; //Zageszczenie siatki
        boolean fill=config.fill; //Wypelnienie tla wejsciowymi kolorami -> jesli fill=false zostanie utworzona tylko siatka, a tlo zostanie usuniete (ustawione na biale)

        int width = image.getWidth();
        int height = image.getHeight();

        //Punkty znajdujace sie na krawedziach ziaren (granice ziaren)
        List<Point> collectors = new ArrayList<>();
        
        //Punkty podzialowe
        List<Point> particles = new ArrayList<>();

        //Detekcja krawedzi bazujac na algorytmie Sobel Edge Detection oraz na podstawie zageszczenia siatki i dodanie punktow krawedzi na tej podstawie
        SobelEdgeDetection.sobel(image, (magnitude, x, y) -> {
            if (magnitude > 1) { //40
                collectors.add(new Point(x, y));
            }
        });

        //Wygenerowanie losowych punktow w obrebie siatki
        for (int i = 0; i < pointCount; ++i) {
            particles.add(new Point((int) (Math.random() * width), (int) (Math.random() * height)));
        }

        //Wylosowanie punktow znajdujacych sie na poszczegolnych krawedziach, na ktorych podstawie bedzie dzialac algorytm Delaunaya
        //Accuracy - zageszczenie , a wiec dokladnosc siatki okresla jak duzo punktow z krawedzi bedzie uczestniczylo w procesie tworzenia siatki
        int length = collectors.size() / accuracy;
        for (int i = 0; i < length; ++i) {
            int random = (int) (Math.random() * collectors.size());
            particles.add(collectors.get(random));
            collectors.remove(random);
        }

        //Punkty narozne krawedzi - calej siatki
        particles.add(new Point(0, 0));
        particles.add(new Point(0, height));
        particles.add(new Point(width, 0));
        particles.add(new Point(width, height));

        //Lista trojkatow w siatce
        List<Integer> triangles = DelaunayTriangulation.triangulate(particles);

        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = out.getGraphics();

        //Zmienne pomocnicze
        double x1, x2, x3, y1, y2, y3, cx, cy;

        //Na podstawie utworzonych trojkatow (3 punkty) pobranie koloru ze srodka i wypelnienie trojkata danym kolorem bazowym z mikrostruktury poczatkowej
        for (int i = 0; i < triangles.size(); i += 3) {
            x1 = particles.get(triangles.get(i)).x;
            x2 = particles.get(triangles.get(i + 1)).x;
            x3 = particles.get(triangles.get(i + 2)).x;
            y1 = particles.get(triangles.get(i)).y;
            y2 = particles.get(triangles.get(i + 1)).y;
            y3 = particles.get(triangles.get(i + 2)).y;

            cx = (x1 + x2 + x3) / 3.0;
            cy = (y1 + y2 + y3) / 3.0;

            Color color = new Color(image.getRGB((int) cx, (int) cy));
            g.setColor(color);

            if (fill) {
                //Wypelnienie kolorem oryginalnym kazdego nowego 'malego trojkata' na siatce powstalego po wykryciu krawedzi (Sobel) i triangulacji (delaunay)
                g.fillPolygon(new int[]{(int) x1, (int) x2, (int) x3}, new int[]{(int) y1, (int) y2, (int) y3}, 3);
                //Narysowanie lini siatki na zdyskretyzowanym obszarze
                g.setColor(Color.BLACK);
                g.drawPolygon(new int[]{(int) x1, (int) x2, (int) x3}, new int[]{(int) y1, (int) y2, (int) y3}, 3);
            } else {
                //Opcja tylko dla samych lini siatki, bez wypelniania kolorami kazdego trojkata powstalego po wykryciu krawedzi (Sobel) i triangulacji (Delaunay)
                g.drawPolygon(new int[]{(int) x1, (int) x2, (int) x3}, new int[]{(int) y1, (int) y2, (int) y3}, 3);
            }
        }
        
        //Dodatki w GUI
        GUI.lTrianglesNumber.setText("Liczba trójkątów siatki: "+triangles.size());
        GUI.lPointsNumber.setText("Liczba punktów siatki: "+particles.size());
        GUI.lTimeGenerated.setText("Czas generowania siatki: "+MeshPanel.meshGeneratingTime+" ms");
        MeshPanel.pointsOnMesh = collectors;
        return out;
    }
        
        
    //Metoda odpowiedzialna za zidentyfikowanie kazdego z ziaren na podstawie punktow znajdujacych sie na krawedziach
    //Opcja bardziej wydajna bo punktow jest znacznie mniej niz jakby sprawdzac kazdy piksel na obrazie czyli 800*800
    //A jest wystarczajco dobra/dokladna bo jest w stanie zidentyfikowac ziarno kazdego koloru na utworzonej siatce
    public static List<Grain> identifyAllGrains(List<Point> pointsToCheck, BufferedImage image){
        List<Grain> grainsList = new ArrayList<>();
        boolean flag;
       
        for (int i=0; i<pointsToCheck.size(); i++){
            flag = false;
            Color c = new Color (image.getRGB(pointsToCheck.get(i).x, pointsToCheck.get(i).y));
            Grain gTmp = new Grain(c.getRed(), c.getGreen(), c.getBlue());
            if(grainsList.isEmpty())
                grainsList.add(gTmp);
            else{
                for (int j=0; j<grainsList.size(); j++){
                   if (areGrainsColorsSimilar(grainsList.get(j), gTmp)) {
                       flag = true;
                       break;
                   }
                }
                if (flag == false)
                    grainsList.add(gTmp);
            }
        }
    
        //System.out.println(grainsList.size());
        return grainsList;
    }
    
    
   //Metoda sprawdzajaca podobienstwo poszczegolnych kolorow w %
   static boolean areGrainsColorsSimilar(Grain g1, Grain g2){
        int percentTolerance = 12; //12% tolerancja dla kolorow RGB, tak zeby zidentyfikowac kazde ziarno
        
        //Pobranie roznicy pomiedzy Red,Green,Blue obu ziaren
        int diffRed   = Math.abs(g1.red   - g2.red);
        int diffGreen = Math.abs(g1.green - g2.green);
        int diffBlue  = Math.abs(g1.blue - g2.blue);
        
        //255 roznych mozliwych wartosci 
        double pctDiffRed   = (double)diffRed   / 255;
        double pctDiffGreen = (double)diffGreen / 255;
        double pctDiffBlue   = (double)diffBlue  / 255; 
        
        //Jezeli srednia procentowa tych 3 wartosci (3 wartosci RGB skladaja sie na kolor w tym rodzaju kodowania kolorow)
        //jest mniejsza niz 10% roznica to znaczy ze kolory sa podobne (ludzkim okiem te same)
        //jest wieksza niz 10% to kolory sa zdecydowanie inne
         if(((pctDiffRed + pctDiffGreen + pctDiffBlue) / 3 * 100) < percentTolerance)
             return true;
         else
             return false;
    }
   
   
   
}

