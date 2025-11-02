package com.bluestaq.dasaria;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingWorker;
import java.util.List;
import java.awt.Toolkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import javax.swing.ImageIcon;
import java.awt.Image;
import javax.imageio.ImageIO;

import javax.swing.Timer;


public class Elevator {

    private JFrame frame;
    private JLabel floorDisplayLabel;
    private JPanel viewPanel;
    private JLabel viewDisplayLabel;

    private int currentFloor;
    private Direction currentDirection;
    private DoorState currentDoorState;
    private boolean isMoving;

    private final String soundResourcePath = "/elevator-dingwav-14913.wav";
    private Clip soundClip;

    private enum Direction {
        UP,
        DOWN,
        IDLE
    }

    private enum DoorState {
        OPEN,
        CLOSED
    }

    public Elevator() {
        currentFloor = 1;
        currentDirection = Direction.IDLE;
        currentDoorState = DoorState.OPEN;
        isMoving = false;

        loadSound();

        frame = new JFrame("Elevator Simulation");
        frame.setSize(500, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel displayPanel = new JPanel(new BorderLayout());
        floorDisplayLabel = new JLabel();
        floorDisplayLabel.setFont(new Font("Arial", Font.BOLD, 22));
        floorDisplayLabel.setHorizontalAlignment(SwingConstants.CENTER);
        floorDisplayLabel.setOpaque(true);
        floorDisplayLabel.setBackground(Color.BLACK);
        floorDisplayLabel.setForeground(Color.GREEN);
        floorDisplayLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        updateFloorDisplay();
        displayPanel.add(floorDisplayLabel, BorderLayout.CENTER);

        JPanel mainContentPanel = new JPanel(new BorderLayout(10, 10));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        viewPanel = new JPanel(new BorderLayout());
        viewDisplayLabel = new JLabel("", SwingConstants.CENTER);
        viewDisplayLabel.setFont(new Font("Serif", Font.ITALIC, 16));
        viewPanel.add(viewDisplayLabel, BorderLayout.CENTER);
        mainContentPanel.add(viewPanel, BorderLayout.CENTER);

        updateViewImage(currentFloor);
        JPanel buttonPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        Font numberFont = new Font("Arial", Font.BOLD, 18);
        Font symbolFont = new Font("Arial", Font.BOLD, 18);
        Font emojiFont = new Font("Dialog", Font.PLAIN, 24);

        ActionListener floorListener = (ActionEvent e) -> {
            JButton source = (JButton) e.getSource();
            int requestedFloor = Integer.parseInt(source.getText());

            if (isMoving || requestedFloor == currentFloor) {
                return;
            }

            isMoving = true;
            currentDirection = (requestedFloor > currentFloor) ? Direction.UP : Direction.DOWN;
            currentDoorState = DoorState.CLOSED;
            updateFloorDisplay();

            loadElevatorImage();
            setViewTitle("Elevator View - Moving...");

            ElevatorWorker worker = new ElevatorWorker(requestedFloor);
            worker.execute();
        };

        JButton b1 = new CircularButton("1");
        b1.setFont(numberFont);
        b1.addActionListener(floorListener);
        buttonPanel.add(b1);

        JButton b2 = new CircularButton("2");
        b2.setFont(numberFont);
        b2.addActionListener(floorListener);
        buttonPanel.add(b2);

        JButton b3 = new CircularButton("3");
        b3.setFont(numberFont);
        b3.addActionListener(floorListener);
        buttonPanel.add(b3);

        JButton b4 = new CircularButton("4");
        b4.setFont(numberFont);
        b4.addActionListener(floorListener);
        buttonPanel.add(b4);

        JButton b5 = new CircularButton("5");
        b5.setFont(numberFont);
        b5.addActionListener(floorListener);
        buttonPanel.add(b5);

        buttonPanel.add(new JPanel());

        JButton bOpen = new CircularButton("<|>");
        bOpen.setFont(symbolFont);
        bOpen.addActionListener((ActionEvent e) -> {
            if (isMoving || currentDoorState == DoorState.OPEN) {
                return;
            }
            currentDoorState = DoorState.OPEN;
            updateViewImage(currentFloor);
        });
        buttonPanel.add(bOpen);

        JButton bClose = new CircularButton(">|<");
        bClose.setFont(symbolFont);
        bClose.addActionListener((ActionEvent e) -> {
            if (isMoving || currentDoorState == DoorState.CLOSED) {
                return;
            }
            currentDoorState = DoorState.CLOSED;
            loadElevatorImage();
            setViewTitle("Elevator View - Idle");
        });
        buttonPanel.add(bClose);

        JButton bEmergency = new CircularButton("📞");
        bEmergency.setFont(emojiFont);
        bEmergency.setBackground(Color.RED);
        bEmergency.setForeground(Color.WHITE);
        bEmergency.addActionListener((ActionEvent e) -> {
            frame.dispose();
        });
        buttonPanel.add(bEmergency);

        buttonPanel.add(new JPanel());

        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.add(displayPanel, BorderLayout.NORTH);
        leftPanel.add(buttonPanel, BorderLayout.CENTER);
        mainContentPanel.add(leftPanel, BorderLayout.WEST);

        frame.add(mainContentPanel, BorderLayout.CENTER);
    }

    private void loadSound() {
        try {

            InputStream audioSrc = getClass().getResourceAsStream(soundResourcePath);
            if (audioSrc == null) {
                System.err.println("Warning: Sound file not found at: " + soundResourcePath);
                soundClip = null;
                return;
            }
            
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedIn);
            
            soundClip = AudioSystem.getClip();
            soundClip.open(audioIn);

        } catch (Exception e) {
            System.err.println("Error loading sound: " + e.getMessage());
            soundClip = null;
        }
    }

    private void playBeepSound() {
        if (soundClip != null) {
            soundClip.stop();
            soundClip.setFramePosition(0);
            soundClip.start();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }


    private void setViewTitle(String title) {
        viewPanel.setBorder(BorderFactory.createTitledBorder(title));
    }


    private void loadElevatorImage() {

        String resourcePath = "/Elevator.jpg";
        
        try (InputStream imgStream = getClass().getResourceAsStream(resourcePath)) {
            if (imgStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            
            Image originalImage = ImageIO.read(imgStream);

            int newWidth = 300;
            int newHeight = (int) (originalImage.getHeight(null) * ((double) newWidth / originalImage.getWidth(null)));
            Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

            viewDisplayLabel.setIcon(new ImageIcon(scaledImage));
            viewDisplayLabel.setText(null);

        } catch (IOException e) {
            viewDisplayLabel.setIcon(null);
            viewDisplayLabel.setText("Error loading 'Moving' image.");
        }
    }


    private void updateFloorDisplay() {
        String displayText = String.valueOf(currentFloor);
        if (currentDirection == Direction.UP) {
            displayText += " ▲";
        } else if (currentDirection == Direction.DOWN) {
            displayText += " ▼";
        }
        floorDisplayLabel.setText(displayText);
    }


    private void updateViewImage(int floorNumber) {
        setViewTitle("Elevator View - Floor " + floorNumber);


        String resourcePath = "/Floor" + floorNumber + ".jpg";

        try (InputStream imgStream = getClass().getResourceAsStream(resourcePath)) {
            if (imgStream == null) {
                 throw new IOException("Resource not found: " + resourcePath);
            }

            Image originalImage = ImageIO.read(imgStream);

            int newWidth = 300;
            int newHeight = (int) (originalImage.getHeight(null) * ((double) newWidth / originalImage.getWidth(null)));

            Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

            viewDisplayLabel.setIcon(new ImageIcon(scaledImage));
            viewDisplayLabel.setText(null);

        } catch (IOException e) {
            viewDisplayLabel.setIcon(null);
            viewDisplayLabel.setText("View for Floor " + floorNumber + " not found.");
        }
    }


    public void show() {
        frame.setVisible(true);
    }


    private class ElevatorWorker extends SwingWorker<Void, Integer> {
        private final int targetFloor;
        private final int step;

        public ElevatorWorker(int targetFloor) {
            this.targetFloor = targetFloor;
            this.step = (targetFloor > currentFloor) ? 1 : -1;
        }

        @Override
        protected Void doInBackground() throws Exception {
            int movingToFloor = currentFloor + step;

            while (true) {
                Thread.sleep(4000);
                publish(movingToFloor);
                if (movingToFloor == targetFloor) {
                    break;
                }
                movingToFloor += step;
            }
            return null;
        }

        @Override
        protected void process(List<Integer> chunks) {
            int newFloor = chunks.get(chunks.size() - 1);
            currentFloor = newFloor;
            updateFloorDisplay();
        }

        @Override
        protected void done() {
            currentDirection = Direction.IDLE;
            isMoving = false;
            updateFloorDisplay();

            playBeepSound();

            Timer imageUpdateTimer = new Timer(4000, (ActionEvent e) -> {
                currentDoorState = DoorState.OPEN;
                updateViewImage(currentFloor);
            });
            imageUpdateTimer.setRepeats(false);
            imageUpdateTimer.start();
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Elevator elevator = new Elevator();
            elevator.show();
        });
    }
}

