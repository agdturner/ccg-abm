/*
 * Copyright 2021 Centre for Computational Geography, University of Leeds.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.leeds.ccg.abm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A GUI for the model.
 *
 * @author Andy Turner
 * @version 1.0
 */
public class GUI {

    /**
     * A reference to the model.
     */
    private final Model model;

    /**
     * A reference to the model.
     */
    private boolean environmentInitialised;

    /**
     * The frame for holding all the GUI components.
     */
    private JFrame frame;

    /**
     * A panel for setting parameters.
     */
    private SetParameters setParameters;

    /**
     * A panel for Start/Stop.
     */
    private StartStop startStop;

    /**
     * A panel for the animation.
     */
    private Animation animation;
    
    /**
     * For increasing the size of the pixel on the screen.
     */
    private int multiplier = 4;
    
    /**
     * Create a new instance.
     * 
     * @param model A reference to the model. 
     */
    public GUI(Model model) {
        this.model = model;
        environmentInitialised = false;
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException
                    | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace(System.err);
            }
            frame = new JFrame("ABM");
            GridBagLayout layout = new GridBagLayout();
            frame.setLayout(layout);
            GridBagConstraints c = layout.getConstraints(frame);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // Set Parameters
            setParameters = new SetParameters();
            c.gridx = 0;
            c.gridy = 0;
            c.gridheight = 1;
            frame.add(setParameters, c);
            setParameters.setVisible(true);
            // Start/Stop
            startStop = new StartStop();
            c.gridx = 0;
            c.gridy = 1;
            c.gridheight = 1;
            frame.add(startStop, c);
            startStop.setVisible(true);
            // Animation
            animation = new Animation();
            c.gridx = 1;
            c.gridy = 0;
            c.gridheight = 2;
            //c.gridwidth =
            frame.add(animation, c);
            animation.setVisible(true);
            frame.pack();
            frame.setLocation(0, 0);
            frame.setVisible(true);
        });
    }

    static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
        BufferedImage bufferedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        bufferedImage.getGraphics()
            .drawImage(resultingImage, 0, 0, null);
        return bufferedImage;
    }
    
    /**
     * A JPanel for storing and presenting the model animation.
     */
    class Animation extends JPanel {

        private static final long serialVersionUID = 1L;

        /**
         * If {@code true} then the animation has started.
         */
        boolean started;

        /**
         * If {@code true} then the model run is complete.
         */
        boolean completed;

        /**
         * If {@code true} then the model is running.
         */
        boolean running;

        /**
         * If {@code true} then the model is paused.
         */
        boolean paused;

        /**
         * Creates a new instance
         */
        private Animation() {
            started = false;
            completed = false;
            running = false;
            paused = false;
        }

        /**
         * Invoke to progress the model.
         */
        private void animate() {
            if (running && !completed) {
                Timer timer = new Timer(40, (ActionEvent e) -> {
                    if (paused) {
                        return;
                    }
                    running = model.iterate(model.stats);
                    if (!running) {
                        completed = true;
                        System.out.println("...animation completed!");
                        return;
                    }
                    //repaint();
                    paintComponent(getGraphics());
                });
                timer.start();
            }
        }

        @Override
        public Dimension getPreferredSize() {
            if (environmentInitialised) {
                int w = ((model.parameters.ncols * multiplier) + getInsets().left + getInsets().right);
                int h = ((model.parameters.nrows * multiplier) + getInsets().top + getInsets().bottom);
                return new Dimension(w, h);
            } else {
                return new Dimension(500, 500);
            }
        }
        
        

        @Override
        protected void paintComponent(Graphics g) {
            //super.paintComponents(g);
            Graphics2D g2d = (Graphics2D) g.create();
            if (environmentInitialised) {
                Image image = null;
                try {
                    image = model.environment.getDataAsImage();
                } catch (Exception ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (image != null) {
                    g2d.drawImage(image, 0, 0, model.parameters.ncols * multiplier, model.parameters.nrows * multiplier, frame);
                }
                for (Grazer gr : model.environment.grazers) {
                    g2d.setColor(Color.GREEN);
                    g2d.drawOval((gr.getCol() - 1) * multiplier, (gr.getRow() - 1) * multiplier, 2, 2);
                    g2d.setColor(Color.BLACK);
                }
            }
            g2d.dispose();
        }
    }

    /**
     * A JPanel for storing the GUI control buttons and for setting parameters.
     */
    class SetParameters extends JPanel implements ChangeListener, ActionListener {

        private static final long serialVersionUID = 1L;

        /**
         * slider, label, text field for the number of iterations.
         */
        SLT iSLT;

        /**
         * slider, label, text field for the number of grazers.
         */
        SLT gSLT;

        /**
         * slider, label, text field for the number of rows.
         */
        SLT nrowsSLT;

        /**
         * slider, label, text field for the number of columns.
         */
        SLT ncolsSLT;

        /**
         * For disabling setting of parameters.
         */
        boolean disabled = false;

        private SetParameters() {
            super(new GridBagLayout());
            initComponents();
        }

        /**
         * For initialising the components.
         */
        private void initComponents() {
            GridBagLayout layout = (GridBagLayout) this.getLayout();
            GridBagConstraints c = layout.getConstraints(this);

            /**
             * 1. iterations
             */
            iSLT = new SLT(this, c, 0, 0,
                    model.parameterConstraints.minNIterations,
                    model.parameterConstraints.maxNIterations,
                    model.parameters.nIterations, "Number of Iterations");
            /**
             * 2. nGrazers
             */
            gSLT = new SLT(this, c, 0, 2,
                    model.parameterConstraints.minInitialNGrazers,
                    model.parameterConstraints.maxInitialNGrazers,
                    model.parameters.initialNGrazers, "Number of Grazers");
            /**
             * 3. nrows
             */
            nrowsSLT = new SLT(this, c, 0, 4,
                    model.parameterConstraints.minNrows,
                    model.parameterConstraints.maxNrows,
                    model.parameters.nrows, "Number of Rows");
            /**
             * 3. ncols
             */
            ncolsSLT = new SLT(this, c, 0, 6,
                    model.parameterConstraints.minNcols,
                    model.parameterConstraints.maxNcols,
                    model.parameters.ncols, "Number of Columns");
            // Finally
            disabled = false;
        }

        @Override
        public void stateChanged(ChangeEvent evt) {
            if (evt.getSource().equals(iSLT.s)) {
                int i0 = model.parameters.nIterations;
                int i = check(iSLT.s, iSLT.t, iSLT.s.getValue(),
                        model.parameterConstraints.minNIterations,
                        model.parameterConstraints.maxNIterations,
                        "Number of Iterations");
                if (i != i0) {
                    model.parameters.nIterations = i;
//                    System.out.println("nIterations changed to "
//                            + model.parameters.nIterations);
                }
            } else if (evt.getSource().equals(gSLT.s)) {
                int i0 = model.parameters.initialNGrazers;
                int i = check(gSLT.s, gSLT.t, gSLT.s.getValue(),
                        model.parameterConstraints.minInitialNGrazers,
                        model.parameterConstraints.maxInitialNGrazers,
                        "Number of Initial Grazers");
                if (i != i0) {
                    model.parameters.initialNGrazers = i;
//                    System.out.println("initialNGrazers changed to "
//                            + model.parameters.initialNGrazers);
                }
            } else if (evt.getSource().equals(nrowsSLT.s)) {
                int i0 = model.parameters.nrows;
                int i = check(nrowsSLT.s, nrowsSLT.t, 
                        nrowsSLT.s.getValue(),
                        model.parameterConstraints.minNrows,
                        model.parameterConstraints.maxNrows,
                        "Number of Rows");
                if (i != i0) {
                    model.parameters.nrows = i;
//                    System.out.println("nrows changed to "
//                            + model.parameters.nrows);
                }
            } else if (evt.getSource().equals(ncolsSLT.s)) {
                int i0 = model.parameters.ncols;
                int i = check(ncolsSLT.s, ncolsSLT.t, 
                        ncolsSLT.s.getValue(),
                        model.parameterConstraints.minNcols,
                        model.parameterConstraints.maxNcols,
                        "Number of Columns");
                if (i != i0) {
                    model.parameters.ncols = i;
//                    System.out.println("ncols changed to "
//                            + model.parameters.ncols);
                }
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource().equals(iSLT.t)) {
                int i0 = model.parameters.nIterations;
                int i = check(iSLT.s, iSLT.t, 
                        Integer.valueOf(iSLT.t.getText()),
                        model.parameterConstraints.minNIterations,
                        model.parameterConstraints.maxNIterations,
                        "Number of Iterations");
                if (i != i0) {
                    model.parameters.nIterations = i;
//                    System.out.println("nIterations changed to "
//                            + model.parameters.nIterations);
                }
            } else if (e.getSource().equals(gSLT.t)) {
                int i0 = model.parameters.initialNGrazers;
                int i = check(gSLT.s, gSLT.t, 
                        Integer.valueOf(gSLT.t.getText()),
                        model.parameterConstraints.minInitialNGrazers,
                        model.parameterConstraints.maxInitialNGrazers,
                        "Number of Initial Grazers");
                if (i != i0) {
                    model.parameters.initialNGrazers = i;
//                    System.out.println("nGrazers changed to "
//                            + model.parameters.initialNGrazers);
                }
            } else if (e.getSource().equals(nrowsSLT.t)) {
                int i0 = model.parameters.nrows;
                int i = check(nrowsSLT.s, nrowsSLT.t, 
                        Integer.valueOf(nrowsSLT.t.getText()),
                        model.parameterConstraints.minNrows,
                        model.parameterConstraints.maxNrows,
                        "Number of Rows");
                if (i != i0) {
                    model.parameters.nrows = i;
//                    System.out.println("nrows changed to "
//                            + model.parameters.nrows);
                }
            } else if (e.getSource().equals(ncolsSLT.t)) {
                int i0 = model.parameters.initialNGrazers;
                int i = check(ncolsSLT.s, ncolsSLT.t, 
                        Integer.valueOf(ncolsSLT.t.getText()),
                        model.parameterConstraints.minNcols,
                        model.parameterConstraints.maxNcols,
                        "Number of Columns");
                if (i != i0) {
                    model.parameters.ncols = i;
//                    System.out.println("ncols changed to "
//                            + model.parameters.ncols);
                }
            }
        }

        /**
         * An inner class that groups together a label, slider and text field
         * and adds the components to the SetParameters layout.
         */
        private class SLT {

            /**
             * The label.
             */
            JLabel l;

            /**
             * The slider.
             */
            JSlider s;

            /**
             * The text field.
             */
            JTextField t;

            /**
             * Create a new instance.
             *
             * @param p The parent.
             * @param c The layout constraints.
             * @param x The x offset for adding to the layout.
             * @param y The y offset for adding to the layout.
             * @param smin slider min.
             * @param smax slider max.
             * @param v value.
             * @param lt label text.
             */
            private SLT(SetParameters p, GridBagConstraints c, int x, int y,
                    int smin, int smax, int v, String lt) {
                l = new JLabel();
                l.setText(lt);
                s = new JSlider(smin, smax, v);
                s.setMinorTickSpacing(smax / 10);
                s.setMajorTickSpacing(smin / 2);
                s.setPaintTicks(true);
                //s.setPaintLabels(true);
                s.setMinimum(smin);
                s.setMaximum(smax);
                s.addChangeListener(p);
                s.setVisible(true);
                t = new JTextField(10);
                t.setText(Integer.toString(v));
                t.addActionListener(p);
                // Add components to p layout
                c.gridx = x;
                c.gridy = y;
                c.gridwidth = 2;
                add(l, c);
                c.gridx = x;
                c.gridy = y + 1;
                c.gridwidth = 1;
                add(s, c);
                c.gridx = x + 1;
                c.gridy = y + 1;
                c.gridwidth = 1;
                add(t, c);
            }
        }
    }

    /**
     * A JPanel for storing the GUI control buttons and for setting parameters.
     */
    private class StartStop extends JPanel implements ActionListener {

        private static final long serialVersionUID = 1L;

        /**
         * The Start/Pause button.
         */
        private final JButton startPauseButton;

        private StartStop() {
            // Start/Pause button
            startPauseButton = new JButton("Start/Pause");
            startPauseButton.setEnabled(true);
            startPauseButton.addActionListener(this);
            startPauseButton.setVisible(true);
            add(startPauseButton, -1);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource().equals(startPauseButton)) {
                if (!setParameters.disabled) {
                    setParameters.iSLT.s.setEnabled(false);
                    setParameters.iSLT.t.setEditable(false);
                    setParameters.gSLT.s.setEnabled(false);
                    setParameters.gSLT.t.setEditable(false);
                    setParameters.nrowsSLT.s.setEnabled(false);
                    setParameters.nrowsSLT.t.setEditable(false);
                    setParameters.ncolsSLT.s.setEnabled(false);
                    setParameters.ncolsSLT.t.setEditable(false);
                    setParameters.disabled = true;
                }
                if (environmentInitialised == false) {
                    model.environment = new Environment(model.parameters,
                            model.factory);
                    environmentInitialised = true;
                    animation.running = true;
                    System.out.println("Animation starting...");
                    animation.animate();
                } else {
                    System.out.println("Start/Pause button pressed.");
                    if (animation.running) {
                        if (animation.paused) {
                            animation.paused = false;
                            System.out.println("Animation unpaused...");
                            animation.animate();
                        } else {
                            animation.paused = true;
                            System.out.println("Animation paused...");
                        }
                    }
                }
            }
        }

//        @Override
//        protected void paintComponent(Graphics g) {
//            super.paintComponents(g);
//        }
    }

    /**
     * Constrain the value for nIterations.
     *
     * @param s The slider.
     * @param t The text field.
     * @param i The value.
     * @return The constrained value.
     */
    private int check(JSlider s, JTextField t, int i, int min, int max,
            String st) {
        if (i < min) {
            i = min;
            System.out.println("Minimum " + st + " is " + i);
        }
        if (i > max) {
            i = max;
            System.out.println("Maximum " + st + " is " + i);
        }
        s.setValue(i);
        t.setText(Integer.toString(i));
        return i;
    }
}
