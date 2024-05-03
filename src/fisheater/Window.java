
package fisheater;

import javax.swing.JFrame;

public class Window extends JFrame {
    private final int WIDTH = 1280;
    private final int HEIGHT = 720;

    public Window() {
        add(new Board(WIDTH, HEIGHT));

        setTitle("Fish Eater");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
    }

    public static void main(String[] args) {
        new Window();
    }

}
