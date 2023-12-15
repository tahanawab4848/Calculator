import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;
import java.util.Stack;

// ==================== THEME SYSTEM ====================
enum Theme {
    NEON("Neon Night", new Color(20, 20, 40), new Color(30, 30, 60), 
         new Color(0, 255, 255), new Color(255, 0, 255), new Color(0, 255, 128)),
    SUNSET("Sunset Glow", new Color(60, 20, 40), new Color(90, 30, 60),
           new Color(255, 140, 0), new Color(255, 20, 147), new Color(255, 215, 0)),
    OCEAN("Ocean Breeze", new Color(10, 40, 60), new Color(20, 60, 90),
          new Color(0, 200, 255), new Color(0, 255, 200), new Color(100, 200, 255)),
    FOREST("Forest Mist", new Color(20, 40, 20), new Color(30, 60, 30),
           new Color(50, 205, 50), new Color(144, 238, 144), new Color(34, 139, 34)),
    MINIMAL("Minimal Light", new Color(240, 240, 240), new Color(220, 220, 220),
            new Color(80, 80, 80), new Color(100, 100, 100), new Color(60, 60, 60)),
    DARK("Dark Elegance", new Color(30, 30, 30), new Color(45, 45, 45),
         new Color(200, 200, 200), new Color(255, 255, 255), new Color(150, 150, 150)),
    CANDY("Candy Pop", new Color(255, 240, 245), new Color(255, 228, 235),
          new Color(255, 105, 180), new Color(255, 20, 147), new Color(255, 182, 193)),
    CYBERPUNK("Cyberpunk", new Color(10, 0, 30), new Color(20, 0, 50),
              new Color(255, 0, 80), new Color(0, 255, 255), new Color(255, 255, 0));

    final String name;
    final Color bgColor, displayBg, accent1, accent2, accent3;
    
    Theme(String name, Color bg, Color disp, Color a1, Color a2, Color a3) {
        this.name = name;
        this.bgColor = bg;
        this.displayBg = disp;
        this.accent1 = a1;
        this.accent2 = a2;
        this.accent3 = a3;
    }
}

// ==================== CALCULATOR MODES ====================
enum CalcMode {
    SIMPLE, SCIENTIFIC, PROGRAMMER, GRAPHING, FINANCIAL, UNIT_CONVERTER, DATE_CALC, BMI_CALC, DISCOUNT_CALC, TIP_CALC
}

// ==================== MAIN CALCULATOR CLASS ====================
public class Calculator extends JFrame {
    private Theme currentTheme = Theme.NEON;
    private CalcMode currentMode = CalcMode.SIMPLE;
    private JPanel mainPanel, displayPanel, buttonPanel, modePanel;
    private JTextField display, historyDisplay;
    private String currentInput = "0";
    private String previousInput = "";
    private String operator = "";
    private boolean startNewInput = true;
    private boolean angleInDegrees = true;
    private int programmerBase = 10;
    private Stack<String> history = new Stack<>();
    private Map<String, JButton> buttons = new HashMap<>();
    private List<String> historyList = new ArrayList<>();
    private int historyIndex = -1;
    
    // Financial variables
    private double interestRate = 0;
    private int periods = 0;
    private double principal = 0;
    
    // Unit converter
    private JComboBox<String> unitFrom, unitTo, unitCategory;
    private JTextField unitInput, unitOutput;
    
    // Graphing
    private JPanel graphPanel;
    private List<Point> graphPoints = new ArrayList<>();
    
    public Calculator() {
        setTitle("✨ Ultimate Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 700);
        setLocationRelativeTo(null);
        setResizable(true);
        
        applyTheme();
        setupUI();
        setupKeyboardShortcuts();
    }
    
    private void applyTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Mode selector
        modePanel = new JPanel(new FlowLayout());
        modePanel.setBackground(currentTheme.bgColor);
        String[] modes = {"Simple", "Scientific", "Programmer", "Graphing", "Financial", "Converter", "Date", "BMI", "Discount", "Tip"};
        for (String m : modes) {
            JButton btn = createStyledButton(m, currentTheme.accent2, 100, 30);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.addActionListener(e -> switchMode(CalcMode.valueOf(m.toUpperCase().replace("CONVERTER", "UNIT_CONVERTER").replace("DATE", "DATE_CALC").replace("BMI", "BMI_CALC").replace("DISCOUNT", "DISCOUNT_CALC").replace("TIP", "TIP_CALC"))));
            modePanel.add(btn);
        }
        add(modePanel, BorderLayout.NORTH);
        
        // Main content
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(currentTheme.bgColor);
        
        // Display
        displayPanel = new JPanel(new BorderLayout(5, 5));
        displayPanel.setBackground(currentTheme.bgColor);
        displayPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        historyDisplay = new JTextField("");
        historyDisplay.setEditable(false);
        historyDisplay.setBackground(currentTheme.displayBg);
        historyDisplay.setForeground(currentTheme.accent3);
        historyDisplay.setFont(new Font("Consolas", Font.PLAIN, 14));
        historyDisplay.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        historyDisplay.setHorizontalAlignment(JTextField.RIGHT);
        
        display = new JTextField("0");
        display.setEditable(false);
        display.setBackground(currentTheme.displayBg);
        display.setForeground(currentTheme.accent1);
        display.setFont(new Font("Segoe UI", Font.BOLD, 48));
        display.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(currentTheme.accent1, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setCaretColor(currentTheme.accent1);
        
        displayPanel.add(historyDisplay, BorderLayout.NORTH);
        displayPanel.add(display, BorderLayout.CENTER);
        
        // Theme selector
        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        themePanel.setBackground(currentTheme.bgColor);
        for (Theme t : Theme.values()) {
            JButton tBtn = new JButton("●");
            tBtn.setForeground(t.accent1);
            tBtn.setBackground(currentTheme.bgColor);
            tBtn.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            tBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            tBtn.setToolTipText(t.name);
            tBtn.addActionListener(e -> {
                currentTheme = t;
                refreshTheme();
            });
            themePanel.add(tBtn);
        }
        displayPanel.add(themePanel, BorderLayout.SOUTH);
        
        mainPanel.add(displayPanel, BorderLayout.NORTH);
        
        // Button panel
        buttonPanel = new JPanel();
        buttonPanel.setBackground(currentTheme.bgColor);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
        
        switchMode(CalcMode.SIMPLE);
    }
    
    private JButton createStyledButton(String text, Color color, int w, int h) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, color, 0, getHeight(), 
                    color.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Glow effect
                if (getModel().isRollover()) {
                    g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 15, 15);
                }
                
                // Text
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(w, h));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Animation on press
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                btn.setLocation(btn.getX(), btn.getY() + 2);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                btn.setLocation(btn.getX(), btn.getY() - 2);
            }
        });
        
        buttons.put(text, btn);
        return btn;
    }
    
    private void refreshTheme() {
        getContentPane().setBackground(currentTheme.bgColor);
        modePanel.setBackground(currentTheme.bgColor);
        mainPanel.setBackground(currentTheme.bgColor);
        displayPanel.setBackground(currentTheme.bgColor);
        buttonPanel.setBackground(currentTheme.bgColor);
        
        display.setBackground(currentTheme.displayBg);
        display.setForeground(currentTheme.accent1);
        display.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(currentTheme.accent1, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        historyDisplay.setBackground(currentTheme.displayBg);
        historyDisplay.setForeground(currentTheme.accent3);
        
        // Refresh all buttons
        for (Map.Entry<String, JButton> entry : buttons.entrySet()) {
            JButton btn = entry.getValue();
            String text = entry.getKey();
            Color baseColor = getButtonColor(text);
            btn.setBackground(baseColor);
        }
        
        repaint();
        revalidate();
    }
    
    private Color getButtonColor(String text) {
        if (Arrays.asList("0","1","2","3","4","5","6","7","8","9",".").contains(text))
            return currentTheme.accent1;
        if (Arrays.asList("+","-","×","÷","=","%","^","√","sin","cos","tan","log","ln","π","e","!").contains(text))
            return currentTheme.accent2;
        if (Arrays.asList("C","CE","⌫","AC","DEL").contains(text))
            return new Color(255, 80, 80);
        if (Arrays.asList("(",")","MC","MR","M+","M-","MS","M").contains(text))
            return currentTheme.accent3;
        return currentTheme.accent3;
    }
    
    private void switchMode(CalcMode mode) {
        currentMode = mode;
        buttonPanel.removeAll();
        buttons.clear();
        
        switch (mode) {
            case SIMPLE -> setupSimpleMode();
            case SCIENTIFIC -> setupScientificMode();
            case PROGRAMMER -> setupProgrammerMode();
            case GRAPHING -> setupGraphingMode();
            case FINANCIAL -> setupFinancialMode();
            case UNIT_CONVERTER -> setupConverterMode();
            case DATE_CALC -> setupDateMode();
            case BMI_CALC -> setupBMIMode();
            case DISCOUNT_CALC -> setupDiscountMode();
            case TIP_CALC -> setupTipMode();
        }
        
        refreshTheme();
        revalidate();
        repaint();
    }
    
    // ==================== SIMPLE MODE ====================
    private void setupSimpleMode() {
        buttonPanel.setLayout(new GridLayout(5, 4, 8, 8));
        
        String[] labels = {
            "C", "⌫", "%", "÷",
            "7", "8", "9", "×",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "±", "0", ".", "="
        };
        
        for (String label : labels) {
            JButton btn = createStyledButton(label, getButtonColor(label), 100, 70);
            btn.addActionListener(e -> handleInput(label));
            buttonPanel.add(btn);
        }
    }
    
    // ==================== SCIENTIFIC MODE ====================
    private void setupScientificMode() {
        buttonPanel.setLayout(new GridLayout(7, 5, 6, 6));
        
        String[] labels = {
            "sin", "cos", "tan", "log", "ln",
            "asin", "acos", "atan", "10^x", "e^x",
            "x²", "x³", "x^y", "√", "∛",
            "π", "e", "n!", "1/x", "RAD",
            "(", ")", "C", "⌫", "÷",
            "7", "8", "9", "×", "%",
            "4", "5", "6", "-", "M+",
            "1", "2", "3", "+", "MR",
            "±", "0", ".", "=", "MC"
        };
        
        for (String label : labels) {
            JButton btn = createStyledButton(label, getButtonColor(label), 85, 55);
            btn.addActionListener(e -> handleScientific(label));
            buttonPanel.add(btn);
        }
    }
    
    // ==================== PROGRAMMER MODE ====================
    private void setupProgrammerMode() {
        buttonPanel.setLayout(new BorderLayout(5, 5));
        
        // Base selector
        JPanel basePanel = new JPanel(new FlowLayout());
        basePanel.setBackground(currentTheme.bgColor);
        String[] bases = {"DEC", "HEX", "OCT", "BIN"};
        for (String b : bases) {
            JButton btn = createStyledButton(b, currentTheme.accent2, 80, 35);
            btn.addActionListener(e -> {
                programmerBase = switch(b) {
                    case "HEX" -> 16;
                    case "OCT" -> 8;
                    case "BIN" -> 2;
                    default -> 10;
                };
                updateDisplay();
            });
            basePanel.add(btn);
        }
        buttonPanel.add(basePanel, BorderLayout.NORTH);
        
        // Buttons
        JPanel progButtons = new JPanel(new GridLayout(6, 4, 6, 6));
        progButtons.setBackground(currentTheme.bgColor);
        
        String[] labels = {
            "A", "B", "C", "AND",
            "D", "E", "F", "OR",
            "7", "8", "9", "XOR",
            "4", "5", "6", "NOT",
            "1", "2", "3", "NAND",
            "0", "±", "⌫", "NOR",
            "C", "≪", "≫", "="
        };
        
        for (String label : labels) {
            JButton btn = createStyledButton(label, getButtonColor(label), 100, 60);
            btn.addActionListener(e -> handleProgrammer(label));
            progButtons.add(btn);
        }
        buttonPanel.add(progButtons, BorderLayout.CENTER);
    }
    
    // ==================== GRAPHING MODE ====================
    private void setupGraphingMode() {
        buttonPanel.setLayout(new BorderLayout(5, 5));
        
        // Graph display
        graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth(), h = getHeight();
                int cx = w/2, cy = h/2;
                
                // Grid
                g2.setColor(new Color(50, 50, 50));
                g2.setStroke(new BasicStroke(1));
                for (int i = 0; i < w; i += 40) g2.drawLine(i, 0, i, h);
                for (int i = 0; i < h; i += 40) g2.drawLine(0, i, w, i);
                
                // Axes
                g2.setColor(currentTheme.accent1);
                g2.setStroke(new BasicStroke(2));
                g2.drawLine(0, cy, w, cy);
                g2.drawLine(cx, 0, cx, h);
                
                // Plot points
                if (!graphPoints.isEmpty()) {
                    g2.setColor(currentTheme.accent2);
                    g2.setStroke(new BasicStroke(2));
                    for (int i = 1; i < graphPoints.size(); i++) {
                        Point p1 = graphPoints.get(i-1);
                        Point p2 = graphPoints.get(i);
                        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                }
            }
        };
        graphPanel.setPreferredSize(new Dimension(480, 300));
        graphPanel.setBackground(currentTheme.bgColor);
        buttonPanel.add(graphPanel, BorderLayout.CENTER);
        
        // Controls
        JPanel controls = new JPanel(new GridLayout(2, 5, 5, 5));
        controls.setBackground(currentTheme.bgColor);
        
        String[] funcs = {"sin(x)", "cos(x)", "tan(x)", "x²", "x³", "√x", "1/x", "e^x", "ln(x)", "CLEAR"};
        for (String f : funcs) {
            JButton btn = createStyledButton(f, currentTheme.accent2, 90, 45);
            btn.addActionListener(e -> plotFunction(f));
            controls.add(btn);
        }
        buttonPanel.add(controls, BorderLayout.SOUTH);
    }
    
    private void plotFunction(String func) {
        graphPoints.clear();
        int w = graphPanel.getWidth();
        int h = graphPanel.getHeight();
        int cx = w/2, cy = h/2;
        
        for (int px = 0; px < w; px += 2) {
            double x = (px - cx) / 40.0;
            double y = switch(func) {
                case "sin(x)" -> Math.sin(x);
                case "cos(x)" -> Math.cos(x);
                case "tan(x)" -> Math.tan(x);
                case "x²" -> x * x;
                case "x³" -> x * x * x;
                case "√x" -> x >= 0 ? Math.sqrt(x) : 0;
                case "1/x" -> x != 0 ? 1/x : 0;
                case "e^x" -> Math.exp(x);
                case "ln(x)" -> x > 0 ? Math.log(x) : 0;
                default -> 0;
            };
            
            int py = cy - (int)(y * 40);
            if (py >= 0 && py < h) {
                graphPoints.add(new Point(px, py));
            }
        }
        graphPanel.repaint();
    }
    
    // ==================== FINANCIAL MODE ====================
    private void setupFinancialMode() {
        buttonPanel.setLayout(new GridLayout(6, 3, 8, 8));
        
        String[] labels = {
            "PV", "FV", "PMT", "Rate",
            "Nper", "IRR", "NPV", "ROI",
            "Compound", "Simple", "Amort", "Break",
            "7", "8", "9", "C",
            "4", "5", "6", "⌫",
            "1", "2", "3", "=",
            "0", ".", "±", "AC"
        };
        
        for (String label : labels) {
            JButton btn = createStyledButton(label, getButtonColor(label), 120, 65);
            btn.addActionListener(e -> handleFinancial(label));
            buttonPanel.add(btn);
        }
    }
    
    // ==================== UNIT CONVERTER ====================
    private void setupConverterMode() {
        buttonPanel.setLayout(new BorderLayout(10, 10));
        
        JPanel top = new JPanel(new GridLayout(4, 2, 10, 10));
        top.setBackground(currentTheme.bgColor);
        
        String[] categories = {"Length", "Weight", "Temperature", "Volume", "Area", "Speed", "Time", "Data"};
        unitCategory = new JComboBox<>(categories);
        unitCategory.setBackground(currentTheme.displayBg);
        unitCategory.setForeground(currentTheme.accent1);
        unitCategory.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        top.add(createLabel("Category:"));
        top.add(unitCategory);
        
        unitFrom = new JComboBox<>(new String[]{"m", "km", "cm", "mm", "ft", "in", "mi"});
        unitTo = new JComboBox<>(new String[]{"m", "km", "cm", "mm", "ft", "in", "mi"});
        styleCombo(unitFrom);
        styleCombo(unitTo);
        
        top.add(createLabel("From:"));
        top.add(unitFrom);
        top.add(createLabel("To:"));
        top.add(unitTo);
        
        unitCategory.addActionListener(e -> updateUnitCategories());
        
        unitInput = new JTextField("1");
        unitInput.setFont(new Font("Segoe UI", Font.BOLD, 24));
        unitInput.setBackground(currentTheme.displayBg);
        unitInput.setForeground(currentTheme.accent1);
        unitInput.setHorizontalAlignment(JTextField.CENTER);
        
        unitOutput = new JTextField("");
        unitOutput.setEditable(false);
        unitOutput.setFont(new Font("Segoe UI", Font.BOLD, 24));
        unitOutput.setBackground(currentTheme.displayBg);
        unitOutput.setForeground(currentTheme.accent2);
        unitOutput.setHorizontalAlignment(JTextField.CENTER);
        
        top.add(unitInput);
        top.add(unitOutput);
        
        buttonPanel.add(top, BorderLayout.NORTH);
        
        // Numpad
        JPanel numpad = new JPanel(new GridLayout(4, 3, 8, 8));
        numpad.setBackground(currentTheme.bgColor);
        for (String s : new String[]{"7","8","9","4","5","6","1","2","3","C","0","Convert"}) {
            JButton btn = createStyledButton(s, s.equals("Convert") ? currentTheme.accent2 : getButtonColor(s), 100, 60);
            btn.addActionListener(e -> {
                if (s.equals("C")) unitInput.setText("");
                else if (s.equals("Convert")) convertUnits();
                else unitInput.setText(unitInput.getText() + s);
            });
            numpad.add(btn);
        }
        buttonPanel.add(numpad, BorderLayout.CENTER);
    }
    
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(currentTheme.accent1);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return lbl;
    }
    
    private void styleCombo(JComboBox<String> combo) {
        combo.setBackground(currentTheme.displayBg);
        combo.setForeground(currentTheme.accent1);
        combo.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }
    
    private void updateUnitCategories() {
        String cat = (String) unitCategory.getSelectedItem();
        String[] units = switch(cat) {
            case "Length" -> new String[]{"m", "km", "cm", "mm", "ft", "in", "mi", "yd", "nm"};
            case "Weight" -> new String[]{"kg", "g", "mg", "lb", "oz", "ton", "st"};
            case "Temperature" -> new String[]{"°C", "°F", "K"};
            case "Volume" -> new String[]{"L", "mL", "gal", "qt", "pt", "cup", "fl oz"};
            case "Area" -> new String[]{"m²", "km²", "ft²", "ac", "ha"};
            case "Speed" -> new String[]{"m/s", "km/h", "mph", "kn", "ft/s"};
            case "Time" -> new String[]{"s", "min", "h", "d", "wk", "mo", "yr"};
            case "Data" -> new String[]{"B", "KB", "MB", "GB", "TB", "PB"};
            default -> new String[]{};
        };
        unitFrom.removeAllItems();
        unitTo.removeAllItems();
        for (String u : units) {
            unitFrom.addItem(u);
            unitTo.addItem(u);
        }
    }
    
    private void convertUnits() {
        try {
            double val = Double.parseDouble(unitInput.getText());
            String from = (String) unitFrom.getSelectedItem();
            String to = (String) unitTo.getSelectedItem();
            String cat = (String) unitCategory.getSelectedItem();
            
            double result = UnitConverter.convert(val, from, to, cat);
            unitOutput.setText(String.format("%.6f", result));
        } catch (Exception e) {
            unitOutput.setText("Error");
        }
    }
    
    // ==================== DATE CALCULATOR ====================
    private void setupDateMode() {
        buttonPanel.setLayout(new GridLayout(6, 2, 10, 10));
        
        JTextField date1 = new JTextField("2026-06-18");
        JTextField date2 = new JTextField("2026-12-25");
        JTextField daysAdd = new JTextField("30");
        
        for (JTextField tf : new JTextField[]{date1, date2, daysAdd}) {
            tf.setFont(new Font("Segoe UI", Font.BOLD, 18));
            tf.setBackground(currentTheme.displayBg);
            tf.setForeground(currentTheme.accent1);
            tf.setHorizontalAlignment(JTextField.CENTER);
        }
        
        buttonPanel.add(createLabel("Date 1 (YYYY-MM-DD):"));
        buttonPanel.add(date1);
        buttonPanel.add(createLabel("Date 2 (YYYY-MM-DD):"));
        buttonPanel.add(date2);
        buttonPanel.add(createLabel("Days to add/sub:"));
        buttonPanel.add(daysAdd);
        
        String[] ops = {"Days Between", "Add Days", "Subtract Days", "Weekday", "Week Number", "Leap Year?"};
        for (String op : ops) {
            JButton btn = createStyledButton(op, currentTheme.accent2, 200, 50);
            btn.addActionListener(e -> handleDate(op, date1, date2, daysAdd));
            buttonPanel.add(btn);
        }
    }
    
    // ==================== BMI CALCULATOR ====================
    private void setupBMIMode() {
        buttonPanel.setLayout(new GridLayout(4, 2, 10, 10));
        
        JTextField height = new JTextField("170");
        JTextField weight = new JTextField("65");
        
        for (JTextField tf : new JTextField[]{height, weight}) {
            tf.setFont(new Font("Segoe UI", Font.BOLD, 18));
            tf.setBackground(currentTheme.displayBg);
            tf.setForeground(currentTheme.accent1);
            tf.setHorizontalAlignment(JTextField.CENTER);
        }
        
        buttonPanel.add(createLabel("Height (cm):"));
        buttonPanel.add(height);
        buttonPanel.add(createLabel("Weight (kg):"));
        buttonPanel.add(weight);
        
        JButton calcBtn = createStyledButton("Calculate BMI", currentTheme.accent2, 200, 50);
        calcBtn.addActionListener(e -> {
            try {
                double h = Double.parseDouble(height.getText()) / 100;
                double w = Double.parseDouble(weight.getText());
                double bmi = w / (h * h);
                String category = bmi < 18.5 ? "Underweight" : bmi < 25 ? "Normal" : bmi < 30 ? "Overweight" : "Obese";
                JOptionPane.showMessageDialog(this, String.format("BMI: %.1f\nCategory: %s", bmi, category), "BMI Result", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(calcBtn);
    }
    
    // ==================== DISCOUNT CALCULATOR ====================
    private void setupDiscountMode() {
        buttonPanel.setLayout(new GridLayout(4, 2, 10, 10));
        
        JTextField originalPrice = new JTextField("100");
        JTextField discountPercent = new JTextField("20");
        
        for (JTextField tf : new JTextField[]{originalPrice, discountPercent}) {
            tf.setFont(new Font("Segoe UI", Font.BOLD, 18));
            tf.setBackground(currentTheme.displayBg);
            tf.setForeground(currentTheme.accent1);
            tf.setHorizontalAlignment(JTextField.CENTER);
        }
        
        buttonPanel.add(createLabel("Original Price:"));
        buttonPanel.add(originalPrice);
        buttonPanel.add(createLabel("Discount (%):"));
        buttonPanel.add(discountPercent);
        
        JButton calcBtn = createStyledButton("Calculate", currentTheme.accent2, 200, 50);
        calcBtn.addActionListener(e -> {
            try {
                double price = Double.parseDouble(originalPrice.getText());
                double percent = Double.parseDouble(discountPercent.getText());
                double saved = price * (percent / 100);
                double finalPrice = price - saved;
                JOptionPane.showMessageDialog(this, String.format("Final Price: %.2f\nYou Saved: %.2f", finalPrice, saved), "Discount Result", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(calcBtn);
    }
    
    // ==================== TIP CALCULATOR ====================
    private void setupTipMode() {
        buttonPanel.setLayout(new GridLayout(5, 2, 10, 10));
        
        JTextField billAmount = new JTextField("50");
        JTextField tipPercent = new JTextField("15");
        JTextField split = new JTextField("1");
        
        for (JTextField tf : new JTextField[]{billAmount, tipPercent, split}) {
            tf.setFont(new Font("Segoe UI", Font.BOLD, 18));
            tf.setBackground(currentTheme.displayBg);
            tf.setForeground(currentTheme.accent1);
            tf.setHorizontalAlignment(JTextField.CENTER);
        }
        
        buttonPanel.add(createLabel("Bill Amount:"));
        buttonPanel.add(billAmount);
        buttonPanel.add(createLabel("Tip (%):"));
        buttonPanel.add(tipPercent);
        buttonPanel.add(createLabel("Split (persons):"));
        buttonPanel.add(split);
        
        JButton calcBtn = createStyledButton("Calculate Tip", currentTheme.accent2, 200, 50);
        calcBtn.addActionListener(e -> {
            try {
                double bill = Double.parseDouble(billAmount.getText());
                double tipP = Double.parseDouble(tipPercent.getText());
                int persons = Integer.parseInt(split.getText());
                
                double tip = bill * (tipP / 100);
                double total = bill + tip;
                double perPerson = total / persons;
                
                JOptionPane.showMessageDialog(this, String.format("Tip: %.2f\nTotal: %.2f\nPer Person: %.2f", tip, total, perPerson), "Tip Result", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(calcBtn);
    }
    
    // ==================== INPUT HANDLERS ====================
    private void handleInput(String cmd) {
        switch (cmd) {
            case "C" -> {
                currentInput = "0";
                previousInput = "";
                operator = "";
                startNewInput = true;
                historyDisplay.setText("");
            }
            case "CE" -> {
                currentInput = "0";
                startNewInput = true;
            }
            case "⌫" -> {
                if (currentInput.length() > 1) currentInput = currentInput.substring(0, currentInput.length()-1);
                else currentInput = "0";
            }
            case "±" -> {
                if (!currentInput.equals("0")) {
                    if (currentInput.startsWith("-")) currentInput = currentInput.substring(1);
                    else currentInput = "-" + currentInput;
                }
            }
            case "." -> {
                if (!currentInput.contains(".")) currentInput += ".";
            }
            case "+", "-", "×", "÷", "%", "^" -> {
                if (!operator.isEmpty()) calculate();
                previousInput = currentInput;
                operator = cmd;
                startNewInput = true;
                historyDisplay.setText(previousInput + " " + operator);
            }
            case "=" -> {
                if (!operator.isEmpty()) {
                    calculate();
                    operator = "";
                }
            }
            default -> {
                if (startNewInput) {
                    currentInput = cmd;
                    startNewInput = false;
                } else {
                    currentInput += cmd;
                }
            }
        }
        updateDisplay();
    }
    
    private void handleScientific(String cmd) {
        double val = parseCurrent();
        double result = 0;
        
        switch (cmd) {
            case "sin" -> result = angleInDegrees ? Math.sin(Math.toRadians(val)) : Math.sin(val);
            case "cos" -> result = angleInDegrees ? Math.cos(Math.toRadians(val)) : Math.cos(val);
            case "tan" -> result = angleInDegrees ? Math.tan(Math.toRadians(val)) : Math.tan(val);
            case "asin" -> result = angleInDegrees ? Math.toDegrees(Math.asin(val)) : Math.asin(val);
            case "acos" -> result = angleInDegrees ? Math.toDegrees(Math.acos(val)) : Math.acos(val);
            case "atan" -> result = angleInDegrees ? Math.toDegrees(Math.atan(val)) : Math.atan(val);
            case "log" -> result = Math.log10(val);
            case "ln" -> result = Math.log(val);
            case "10^x" -> result = Math.pow(10, val);
            case "e^x" -> result = Math.exp(val);
            case "x²" -> result = val * val;
            case "x³" -> result = val * val * val;
            case "x^y" -> {
                handleInput("^");
                return;
            }
            case "√" -> result = Math.sqrt(val);
            case "∛" -> result = Math.cbrt(val);
            case "π" -> result = Math.PI;
            case "e" -> result = Math.E;
            case "n!" -> result = factorial((int)val);
            case "1/x" -> result = 1 / val;
            case "RAD" -> {
                angleInDegrees = !angleInDegrees;
                display.setText(angleInDegrees ? "DEG" : "RAD");
                return;
            }
            case "MC", "MR", "M+", "M-", "MS" -> handleMemory(cmd);
            default -> {
                handleInput(cmd);
                return;
            }
        }
        
        currentInput = formatResult(result);
        startNewInput = true;
        updateDisplay();
    }
    
    private void handleProgrammer(String cmd) {
        try {
            long val = Long.parseLong(currentInput, programmerBase);
            
            switch (cmd) {
                case "C" -> {
                    currentInput = "0";
                    startNewInput = true;
                }
                case "⌫" -> {
                    if (currentInput.length() > 1) currentInput = currentInput.substring(0, currentInput.length()-1);
                    else currentInput = "0";
                }
                case "AND" -> {
                    previousInput = currentInput;
                    operator = "AND";
                    startNewInput = true;
                }
                case "OR" -> {
                    previousInput = currentInput;
                    operator = "OR";
                    startNewInput = true;
                }
                case "XOR" -> {
                    previousInput = currentInput;
                    operator = "XOR";
                    startNewInput = true;
                }
                case "NOT" -> {
                    long res = ~val;
                    currentInput = Long.toString(res, programmerBase).toUpperCase();
                }
                case "NAND" -> {
                    previousInput = currentInput;
                    operator = "NAND";
                    startNewInput = true;
                }
                case "NOR" -> {
                    previousInput = currentInput;
                    operator = "NOR";
                    startNewInput = true;
                }
                case "≪" -> {
                    currentInput = Long.toString(val << 1, programmerBase).toUpperCase();
                }
                case "≫" -> {
                    currentInput = Long.toString(val >> 1, programmerBase).toUpperCase();
                }
                case "=" -> {
                    if (!operator.isEmpty()) {
                        long prev = Long.parseLong(previousInput, programmerBase);
                        long res = switch(operator) {
                            case "AND" -> prev & val;
                            case "OR" -> prev | val;
                            case "XOR" -> prev ^ val;
                            case "NAND" -> ~(prev & val);
                            case "NOR" -> ~(prev | val);
                            default -> val;
                        };
                        currentInput = Long.toString(res, programmerBase).toUpperCase();
                        operator = "";
                    }
                }
                case "±" -> {
                    if (currentInput.startsWith("-")) currentInput = currentInput.substring(1);
                    else currentInput = "-" + currentInput;
                }
                default -> {
                    if (cmd.matches("[0-9A-F]")) {
                        if (startNewInput) {
                            currentInput = cmd;
                            startNewInput = false;
                        } else {
                            currentInput += cmd;
                        }
                    }
                }
            }
            updateDisplay();
        } catch (NumberFormatException e) {
            display.setText("Error");
        }
    }
    
    private void handleFinancial(String cmd) {
        switch (cmd) {
            case "PV", "FV", "PMT", "Rate", "Nper" -> {
                display.setText("Enter " + cmd + ":");
                startNewInput = true;
            }
            case "Compound" -> {
                double r = interestRate / 100;
                double n = periods;
                double p = principal;
                double fv = p * Math.pow(1 + r, n);
                currentInput = formatResult(fv);
                updateDisplay();
            }
            case "Simple" -> {
                double r = interestRate / 100;
                double fv = principal * (1 + r * periods);
                currentInput = formatResult(fv);
                updateDisplay();
            }
            case "Amort" -> {
                double r = interestRate / 100 / 12;
                double n = periods * 12;
                double pmt = principal * (r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
                currentInput = formatResult(pmt);
                updateDisplay();
            }
            case "IRR", "NPV", "ROI", "Break" -> {
                display.setText("Feature: " + cmd);
            }
            default -> handleInput(cmd);
        }
    }
    
    private void handleDate(String op, JTextField d1, JTextField d2, JTextField days) {
        try {
            java.time.LocalDate date1 = java.time.LocalDate.parse(d1.getText());
            java.time.LocalDate date2 = java.time.LocalDate.parse(d2.getText());
            int n = Integer.parseInt(days.getText());
            
            String result = switch(op) {
                case "Days Between" -> String.valueOf(java.time.temporal.ChronoUnit.DAYS.between(date1, date2));
                case "Add Days" -> date1.plusDays(n).toString();
                case "Subtract Days" -> date1.minusDays(n).toString();
                case "Weekday" -> date1.getDayOfWeek().toString();
                case "Week Number" -> String.valueOf(date1.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()));
                case "Leap Year?" -> String.valueOf(date1.isLeapYear());
                default -> "";
            };
            
            JOptionPane.showMessageDialog(this, result, op, JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleMemory(String cmd) {
        // Memory operations implementation
        display.setText("Memory: " + cmd);
    }
    
    private void calculate() {
        double a = Double.parseDouble(previousInput);
        double b = Double.parseDouble(currentInput);
        double result = 0;
        
        switch (operator) {
            case "+" -> result = a + b;
            case "-" -> result = a - b;
            case "×" -> result = a * b;
            case "÷" -> result = b != 0 ? a / b : Double.NaN;
            case "%" -> result = a % b;
            case "^" -> result = Math.pow(a, b);
        }
        
        historyDisplay.setText(previousInput + " " + operator + " " + currentInput + " =");
        currentInput = formatResult(result);
        startNewInput = true;
        updateDisplay();
    }
    
    private double parseCurrent() {
        try {
            return Double.parseDouble(currentInput);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private double factorial(int n) {
        if (n < 0) return Double.NaN;
        if (n > 170) return Double.POSITIVE_INFINITY;
        double result = 1;
        for (int i = 2; i <= n; i++) result *= i;
        return result;
    }
    
    private String formatResult(double value) {
        if (Double.isNaN(value)) return "Error";
        if (Double.isInfinite(value)) return "∞";
        if (value == (long)value) return String.valueOf((long)value);
        return String.valueOf(value);
    }
    
    private void updateDisplay() {
        if (currentMode == CalcMode.PROGRAMMER) {
            display.setText(currentInput.toUpperCase() + " [" + programmerBase + "]");
        } else {
            display.setText(currentInput);
        }
    }
    
    // ==================== KEYBOARD SHORTCUTS ====================
    private void setupKeyboardShortcuts() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_TYPED) {
                char c = e.getKeyChar();
                String cmd = switch(c) {
                    case '0','1','2','3','4','5','6','7','8','9' -> String.valueOf(c);
                    case '.' -> ".";
                    case '+' -> "+";
                    case '-' -> "-";
                    case '*' -> "×";
                    case '/' -> "÷";
                    case '%' -> "%";
                    case '^' -> "^";
                    case '=' -> "=";
                    case '\n' -> "=";
                    case '\b' -> "⌫";
                    case 'c', 'C' -> "C";
                    default -> null;
                };
                if (cmd != null) {
                    if (currentMode == CalcMode.SIMPLE) handleInput(cmd);
                    else if (currentMode == CalcMode.SCIENTIFIC) handleScientific(cmd);
                }
            }
            return false;
        });
    }
    
    // ==================== UNIT CONVERTER CLASS ====================
    static class UnitConverter {
        static double convert(double val, String from, String to, String category) {
            // Convert to base unit first
            double base = switch(category) {
                case "Length" -> switch(from) {
                    case "m" -> val;
                    case "km" -> val * 1000;
                    case "cm" -> val / 100;
                    case "mm" -> val / 1000;
                    case "ft" -> val * 0.3048;
                    case "in" -> val * 0.0254;
                    case "mi" -> val * 1609.344;
                    case "yd" -> val * 0.9144;
                    case "nm" -> val * 1852;
                    default -> val;
                };
                case "Weight" -> switch(from) {
                    case "kg" -> val;
                    case "g" -> val / 1000;
                    case "mg" -> val / 1_000_000;
                    case "lb" -> val * 0.453592;
                    case "oz" -> val * 0.0283495;
                    case "ton" -> val * 1000;
                    case "st" -> val * 6.35029;
                    default -> val;
                };
                case "Temperature" -> switch(from) {
                    case "°C" -> val;
                    case "°F" -> (val - 32) * 5/9;
                    case "K" -> val - 273.15;
                    default -> val;
                };
                case "Volume" -> switch(from) {
                    case "L" -> val;
                    case "mL" -> val / 1000;
                    case "gal" -> val * 3.78541;
                    case "qt" -> val * 0.946353;
                    case "pt" -> val * 0.473176;
                    case "cup" -> val * 0.236588;
                    case "fl oz" -> val * 0.0295735;
                    default -> val;
                };
                case "Area" -> switch(from) {
                    case "m²" -> val;
                    case "km²" -> val * 1_000_000;
                    case "ft²" -> val * 0.092903;
                    case "ac" -> val * 4046.86;
                    case "ha" -> val * 10_000;
                    default -> val;
                };
                case "Speed" -> switch(from) {
                    case "m/s" -> val;
                    case "km/h" -> val / 3.6;
                    case "mph" -> val * 0.44704;
                    case "kn" -> val * 0.514444;
                    case "ft/s" -> val * 0.3048;
                    default -> val;
                };
                case "Time" -> switch(from) {
                    case "s" -> val;
                    case "min" -> val * 60;
                    case "h" -> val * 3600;
                    case "d" -> val * 86400;
                    case "wk" -> val * 604800;
                    case "mo" -> val * 2.628e6;
                    case "yr" -> val * 3.154e7;
                    default -> val;
                };
                case "Data" -> switch(from) {
                    case "B" -> val;
                    case "KB" -> val * 1024;
                    case "MB" -> val * 1_048_576;
                    case "GB" -> val * 1_073_741_824;
                    case "TB" -> val * 1_099_511_627_776L;
                    case "PB" -> val * 1_125_899_906_842_624L;
                    default -> val;
                };
                default -> val;
            };
            
            // Convert from base to target
            return switch(category) {
                case "Length" -> switch(to) {
                    case "m" -> base;
                    case "km" -> base / 1000;
                    case "cm" -> base * 100;
                    case "mm" -> base * 1000;
                    case "ft" -> base / 0.3048;
                    case "in" -> base / 0.0254;
                    case "mi" -> base / 1609.344;
                    case "yd" -> base / 0.9144;
                    case "nm" -> base / 1852;
                    default -> base;
                };
                case "Weight" -> switch(to) {
                    case "kg" -> base;
                    case "g" -> base * 1000;
                    case "mg" -> base * 1_000_000;
                    case "lb" -> base / 0.453592;
                    case "oz" -> base / 0.0283495;
                    case "ton" -> base / 1000;
                    case "st" -> base / 6.35029;
                    default -> base;
                };
                case "Temperature" -> switch(to) {
                    case "°C" -> base;
                    case "°F" -> base * 9/5 + 32;
                    case "K" -> base + 273.15;
                    default -> base;
                };
                case "Volume" -> switch(to) {
                    case "L" -> base;
                    case "mL" -> base * 1000;
                    case "gal" -> base / 3.78541;
                    case "qt" -> base / 0.946353;
                    case "pt" -> base / 0.473176;
                    case "cup" -> base / 0.236588;
                    case "fl oz" -> base / 0.0295735;
                    default -> base;
                };
                case "Area" -> switch(to) {
                    case "m²" -> base;
                    case "km²" -> base / 1_000_000;
                    case "ft²" -> base / 0.092903;
                    case "ac" -> base / 4046.86;
                    case "ha" -> base / 10_000;
                    default -> base;
                };
                case "Speed" -> switch(to) {
                    case "m/s" -> base;
                    case "km/h" -> base * 3.6;
                    case "mph" -> base / 0.44704;
                    case "kn" -> base / 0.514444;
                    case "ft/s" -> base / 0.3048;
                    default -> base;
                };
                case "Time" -> switch(to) {
                    case "s" -> base;
                    case "min" -> base / 60;
                    case "h" -> base / 3600;
                    case "d" -> base / 86400;
                    case "wk" -> base / 604800;
                    case "mo" -> base / 2.628e6;
                    case "yr" -> base / 3.154e7;
                    default -> base;
                };
                case "Data" -> switch(to) {
                    case "B" -> base;
                    case "KB" -> base / 1024;
                    case "MB" -> base / 1_048_576;
                    case "GB" -> base / 1_073_741_824;
                    case "TB" -> base / 1_099_511_627_776L;
                    case "PB" -> base / 1_125_899_906_842_624L;
                    default -> base;
                };
                default -> base;
            };
        }
    }
    
    // ==================== MAIN ====================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Calculator calc = new Calculator();
            calc.setVisible(true);
        });
    }
}