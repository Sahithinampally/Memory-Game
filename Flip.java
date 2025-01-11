import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

// This is a base class for the game.
// It defines how many tiles there are and how many tries the player gets.
abstract class MemoryGame {
    protected int gridSize; // Size of the grid 
    protected int maxTries; // Maximum number of tries allowed
    protected int tries; // Current number of tries left

    public MemoryGame(int gridSize) {
        this.gridSize=gridSize;
        this.maxTries=(gridSize*gridSize)-1; // Player gets as many tries as the number of tiles
        this.tries=maxTries;
    }

    public abstract void initializeGame(); // Setup the game

    public abstract void restartGame(JPanel gridPanel); // Restart the game
}

// This class is the main part of the memory game.
// It manages the buttons, numbers, and game rules.
class FlipGame extends MemoryGame {
    private ArrayList<Integer> numbers; // Holds the numbers for the tiles
    private JButton[][] buttons; // The grid of buttons
    private JButton firstButton; // First button clicked by the player
    private int firstValue; // The number on the first button
    private JLabel triesLabel; // Shows how many tries are left
    private boolean gameOver; // Keeps track if the game is over

    public FlipGame(int gridSize, JLabel triesLabel) {
        super(gridSize);
        this.triesLabel=triesLabel;
        this.gameOver=false; // The game starts in a "not over" state
    }

    // This method sets up the game by shuffling pairs of numbers for the tiles.
    public void initializeGame() {
        numbers=new ArrayList<>();
        // Add pairs of numbers to the list
        for (int i=1;i<=(gridSize*gridSize)/2;i++) {
            numbers.add(i);
            numbers.add(i);
        }
        Collections.shuffle(numbers); // Randomize the order of numbers
        gameOver=false; // Reset the game state
    }

    // Resets the game and updates the grid with shuffled tiles.
    public void restartGame(JPanel gridPanel) {
        tries=maxTries; // Reset tries to the maximum allowed
        triesLabel.setText("Tries: "+tries); // Update the label showing tries
        initializeGame(); // Set up new numbers
        populateGrid(gridPanel); // Place new buttons on the grid
    }

    // This creates the grid of buttons and adds them to the panel.
    public void populateGrid(JPanel gridPanel) {
        gridPanel.removeAll(); // Clear anything that was there before
        gridPanel.setLayout(new GridLayout(gridSize, gridSize, 5, 5)); // Create a grid layout
        buttons=new JButton[gridSize][gridSize]; // Prepare the grid
        firstButton=null; // No button has been clicked yet
        firstValue=-1; // No number has been selected yet

        int index=0; // Keeps track of which number goes to which tile
        for (int i=0;i<gridSize;i++) {
            for (int j=0;j<gridSize;j++) {
                JButton button=createButton(numbers.get(index++)); // Create a button with a number
                buttons[i][j]=button;
                gridPanel.add(button); // Add the button to the grid
            }
        }

        gridPanel.revalidate(); // Update the grid layout
        gridPanel.repaint(); // Refresh the grid visually
    }

    // This method creates a button for a tile and attaches the number to it.
    private JButton createButton(int value) {
        JButton button=new JButton();
        button.setBackground(Color.LIGHT_GRAY); // Set button color
        button.setFont(new Font("Arial", Font.BOLD, 20)); // Make the text easy to read
        button.putClientProperty("value", value); // Store the number for this button
        button.addActionListener(e -> handleClick(button)); // What happens when the button is clicked
        return button;
    }

    // This method handles what happens when a player clicks on a button.
    private void handleClick(JButton button) {
        // Ignore the click if the game is over or if the button has already been clicked
        if (gameOver||!button.getText().isEmpty()||tries<=0) return;

        int value=(int) button.getClientProperty("value"); // Get the number on the button
        button.setText(String.valueOf(value)); // Show the number on the button

        if (firstButton==null) {
            // This is the first button the player clicked
            firstButton=button;
            firstValue=value;
        } else {
            // This is the second button the player clicked
            tries--; // Decrease the number of tries left
            triesLabel.setText("Tries: "+tries); // Update the label

            if (firstValue==value) {
                // The two buttons match
                firstButton.setEnabled(false); // Disable the first button
                button.setEnabled(false); // Disable the second button
                resetFirst(); // Reset for the next turn
            } else {
                // The two buttons don't match, flip them back after a short delay
                Timer timer=new Timer(500, e -> {
                    firstButton.setText("");
                    button.setText("");
                    resetFirst(); // Reset for the next turn
                });
                timer.setRepeats(false); // Only run the timer once
                timer.start();
            }

            if (tries<=0) {
                // No more tries left, the game is over
                gameOver=true;
                JOptionPane.showMessageDialog(null, "Game Over! Maximum tries reached.");
                disableAllButtons(); // Disable all buttons
                return; // Stop further actions
            }
        }

        if (isGameComplete()) {
            // All pairs are matched, the player wins!
            gameOver=true;
            JOptionPane.showMessageDialog(null, "Congratulations, you won!");
        }
    }

    // Resets the first button and value so the player can start a new pair.
    private void resetFirst() {
        firstButton=null;
        firstValue=-1;
    }

    // Disables all buttons when the game is over.
    private void disableAllButtons() {
        for (JButton[] row:buttons) {
            for (JButton btn:row) {
                btn.setEnabled(false);
            }
        }
    }

    // Checks if all pairs are matched.
    private boolean isGameComplete() {
        if (gameOver) return false; // Skip if the game is already over
        for (JButton[] row:buttons) {
            for (JButton btn:row) {
                if (btn.isEnabled()) {
                    return false; // A button is still enabled, so the game isn't complete
                }
            }
        }
        return true; // All buttons are matched
    }
}

// This is the main class where the game starts.
public class Flip {
    public static void main(String[] args) {

        // Show a popup with instructions for the game
        JOptionPane.showMessageDialog(null, 
        "Welcome to the Memory Game!\n\n" +
        "The goal is to match pairs of identical numbers.\n" +
        "You have a limited number of tries.\n" +
        "Click on a tile to flip it. Try to find matching pairs!\n\n" +
        "Good luck!");

        JFrame frame=new JFrame("Memory Game"); // Create a new window
        frame.setSize(600, 600); // Set the window size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close the game when the window is closed
        frame.setLayout(new BorderLayout()); // Use a border layout

        JLabel triesLabel=new JLabel("Tries: 0", SwingConstants.CENTER); // Label to show tries left
        JPanel topPanel=new JPanel(new GridLayout(2, 1)); // Panel for instructions and tries
        topPanel.add(new JLabel("Match identical numbers!", SwingConstants.CENTER));
        topPanel.add(triesLabel);
        frame.add(topPanel, BorderLayout.NORTH); // Add the top panel to the window

        JPanel gridPanel=new JPanel(); // Panel for the grid of tiles
        frame.add(gridPanel, BorderLayout.CENTER); // Add the grid panel to the window

        JPanel bottomPanel=new JPanel(); // Panel for the restart button
        JButton restartButton=new JButton("Restart"); // Button to restart the game
        bottomPanel.add(restartButton);
        frame.add(bottomPanel, BorderLayout.SOUTH); // Add the bottom panel to the window

        FlipGame game=new FlipGame(4, triesLabel); // Create the game with a 4x4 grid
        game.initializeGame();
        game.populateGrid(gridPanel); // Fill the grid with tiles

        restartButton.addActionListener(e -> game.restartGame(gridPanel)); // Restart the game when the button is clicked

        frame.setVisible(true); // Show the game window
    }
}
