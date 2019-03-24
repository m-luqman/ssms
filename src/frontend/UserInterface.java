/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package frontend;
import broker.entry.IaEntry;
import broker.entry.EntryContext;
import broker.entry.AdminStatusEntry;
import broker.entry.QuestionEntry;
import broker.view.TextbookBase;
import broker.entry.StatusEntry;
import broker.view.AvgDifficultyQuery;
import broker.view.TopicBase;
import broker.view.DateFilter;
import broker.view.DifficultyFilter;
import broker.view.COFilter;
import broker.view.SubjectFilter;
import broker.view.SubjectBase;
import broker.view.ModuleFilter;
import broker.view.SubjectsCoveredBase;
import broker.view.SemesterFilter;
import broker.view.QuestionsCoveredBase;
import broker.entry.SyllabusEntry;
import broker.view.AbstractionFilter;
import broker.view.USNFilter;
import broker.view.SectionFilter;
import broker.view.DefaultCoveredBase;
import broker.view.TopicsCoveredBase;
import java.awt.Cursor;
import broker.*;
import broker.view.SectionTableQuery;
import broker.view.StudentTableQuery;
import broker.view.ViewingQuery;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import datechooser.events.CommitEvent;
import datechooser.events.CommitListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JasperViewer;
import javax.swing.SwingWorker;

/**
 *
 * @author drseema
 */
public class UserInterface extends javax.swing.JFrame {
    /**
     * Creates new form Window.
     */
    public UserInterface() {
        gateway=new DBGateway();
        initComponents();
        enterer=new EntryContext(this,gateway);
        entryViewer=new ViewingQuery();
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
    }
    private ViewingQuery viewer=new ViewingQuery().withBase(new DefaultCoveredBase());
    private ViewingQuery entryViewer;
    private final EntryContext enterer;
    private final DBGateway gateway;
    private final Set<JComponent> entryComponents=new HashSet<>();
    private final Set<JComponent> viewingComponents=new HashSet<>();
    private DefaultTableModel detailTableModel=new javax.swing.table.DefaultTableModel(
            new Object[][] {

            },
            new String[] {
                "Topic Name", 
                "Textbook Name", 
                "Page Number", 
                "Question Statement", 
                "Total Marks"
            }
        );
    public String USN;
    
    public void onBubbleClick(String text){
        viewTabbedPane.setSelectedIndex(1);
        subjectComboPopupMenuWillBecomeVisible(null);
        selectItemByString(text,subjectCombo);
    }
    private void selectItemByString(String s,JComboBox cb) {
        for (int i=0; i<cb.getItemCount(); i++) {
            if (((String)cb.getItemAt(i)).equals(s)) {
                cb.setSelectedIndex(i);
                break;
            }
        }
    }
    public void enableOnlyComponents(Set<JComponent> componentSet, boolean truthValue,JComponent ... components){
        
        enableAllComponents(componentSet,!truthValue);
    
        for(JComponent aComponent: componentSet)
            if(Arrays.asList(components).contains(aComponent))
                aComponent.setVisible(truthValue);
    }
    public void enableAllComponents(Set<JComponent> componentSet,boolean truthValue){
        for(JComponent aComponent: componentSet)
            aComponent.setVisible(truthValue);
    }
    
    public void distributeTable(){
        detailTable.clearSelection();
        if(finalDateField1.getText().compareTo(initialDateField1.getText()) > 0){
            LocalDate dateBefore = LocalDate.parse(initialDateField1.getText());
            LocalDate dateAfter = LocalDate.parse(finalDateField1.getText());
            int noOfDays=((int)ChronoUnit.DAYS.between(dateBefore, dateAfter));
            int distributionFactor=(detailTableModel.getRowCount() + noOfDays - 1) / noOfDays;
            if (detailTableModel.getRowCount()>distributionFactor)
                detailTable.setRowSelectionInterval(0, distributionFactor-1);
        }
    }
    private void setCoStatusLabel() {
        if(!CheckHelper.checkEmpty(getCO()))
            coStatusLabel.setText("CO "+getCO()+" average: "+gateway.getSubjectInfoList(viewer.withBase(new AvgDifficultyQuery()).asCompositeViewingQuery()));
        else
            coStatusLabel.setText("");
    }
    private void setCombo(JComboBox comboBox,ViewingQuery viewingQuery) {
        comboBox.removeAllItems();
        comboBox.addItem("unselected");
        for (String nameList : gateway.getSubjectInfoList(viewingQuery.asCompositeViewingQuery())) {
            comboBox.addItem(nameList);
        }
    }   
    
    public String getAbstractionLevel(){
        return (String) abstractionLevelCombo.getSelectedItem();        
    }
    public String getSemester(){
        return (String) semesterCombo.getSelectedItem();    
    }
        public String getSubject(){
        return (String) subjectCombo.getSelectedItem();
    }
        public String getCO(){
        return (String) coCombo.getSelectedItem();
    }
        public String getInitialDate(){
        return initialDateField.getText();
    }
        public String getFinalDate(){
        return finalDateField.getText();
    }
        public String getModule(){
        return (String) moduleCombo.getSelectedItem();
    }
        public String getDifficulty(){
        return (String) difficultyCombo.getSelectedItem();
    }
        public String getSection(){
        return (String) sectionCombo.getSelectedItem();
    }
        public String getUSN(){
        return usnField.getText();
    }

        public String getEntryAbstractionLevel(){
            return (String) entryAbstractionLevelCombo.getSelectedItem();
        }
        
        public String getEntrySemester(){
        return (String) entrySemesterCombo.getSelectedItem();    
    }
        public String getEntryDate(){
            return entryDateField.getText();
        }
        public String getEntryTopicChoice(){
            return (String) entryTopicCombo.getSelectedItem();
        }
        public String getEntryTopicField(){
            return entryTopicField.getText();
        }
        public String getEntryPage(){
            return entryPageField.getText();
        }
        public String getEntryModule(){
            return (String) entryModuleCombo.getSelectedItem();
        }
        public String getEntryDifficulty(){
            return (String) entryDifficultyCombo.getSelectedItem();
        }
        public String getEntryTextbook(){
            return (String) entryTextbookCombo.getSelectedItem();
        }
        public String getEntryInternalMarks(){
            return entryIaMarksField.getText();
        }
        public String getEntryQuestionChoiceValue(){
            return (String) entryQuestionCombo.getSelectedItem();
        }
        public String getEntrySubject(){
            return (String) entrySubjectCombo.getSelectedItem();
        }
        public String getEntryCo(){
            return entryCoField.getText();
        }
        public String getEntryTotalMarks(){
            return entryTotalMarksField.getText();
        }
        class EmptyComparator<T extends Comparable<T>> implements Comparator<T>{
                    @Override
                    public int compare(T o1, T o2) {
                        // sort null last
                        if (o1 == o2) {
                            return 0;
                        }
                        if (detailTable.getRowSorter().getSortKeys().get(0).getSortOrder()==SortOrder.ASCENDING){
                          if (o1 == null) {
                            return 1;
                        }
                          if (o2 == null) {
                            return -1;
                        }
                        }
                        else if (detailTable.getRowSorter().getSortKeys().get(0).getSortOrder()==SortOrder.DESCENDING){
                          if (o1 == null) {
                            return -1;
                        }
                          if (o2 == null) {
                            return 1;
                        }
                        }
                        //else {
                            return o1.compareTo(o2);
                        //}
                    }
                }
    ItemListener subjectComboListener=new ItemListener(){
    @Override
    public void itemStateChanged(ItemEvent e){
        if(e.getStateChange()==ItemEvent.SELECTED){
            //refreshTable();
            viewer=viewer.withInputFilters(new SubjectFilter(getSubject()));
            viewButton.doClick();
        }
    }
};
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        entryFrame = new javax.swing.JFrame();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        entryTypePanel = new org.jdesktop.swingx.JXPanel();
        jLabel2 = new javax.swing.JLabel();
        adminStatusEntryRadio = new javax.swing.JRadioButton();
        statusEntryRadio = new javax.swing.JRadioButton();
        questionEntryRadio = new javax.swing.JRadioButton();
        syllabusEntryRadio = new javax.swing.JRadioButton();
        iaEntryRadio = new javax.swing.JRadioButton();
        entrySemesterPanel = new org.jdesktop.swingx.JXPanel();
        entrySemesterLabel = new javax.swing.JLabel();
        entrySemesterCombo = new javax.swing.JComboBox<>();
        entryDatePanel = new org.jdesktop.swingx.JXPanel();
        entryDateLabel = new javax.swing.JLabel();
        entryDateField = new datechooser.beans.DateChooserCombo();
        entrySubjectPanel = new org.jdesktop.swingx.JXPanel();
        entrySubjectLabel = new javax.swing.JLabel();
        entrySubjectCombo = new javax.swing.JComboBox<>();
        entryTopicPanel = new org.jdesktop.swingx.JXPanel();
        entryTopicComboLabel = new javax.swing.JLabel();
        entryTopicCombo = new javax.swing.JComboBox<>();
        entryModulePanel = new org.jdesktop.swingx.JXPanel();
        entryModuleLabel = new javax.swing.JLabel();
        entryModuleCombo = new javax.swing.JComboBox<>();
        entryCoveredTopicPanel = new org.jdesktop.swingx.JXPanel();
        jLabel1 = new javax.swing.JLabel();
        entryCoveredTopicCombo = new javax.swing.JComboBox<>();
        entryNewTopicPanel = new org.jdesktop.swingx.JXPanel();
        entryTopicFieldLabel = new javax.swing.JLabel();
        entryTopicField = new javax.swing.JTextField();
        entryTextbookPanel = new org.jdesktop.swingx.JXPanel();
        entryTextbookLabel = new javax.swing.JLabel();
        entryTextbookCombo = new javax.swing.JComboBox<>();
        entryPagePanel = new org.jdesktop.swingx.JXPanel();
        entryPageLabel = new javax.swing.JLabel();
        entryPageField = new javax.swing.JTextField();
        entryDifficultyPanel = new org.jdesktop.swingx.JXPanel();
        entryDifficultyLabel = new javax.swing.JLabel();
        entryDifficultyCombo = new javax.swing.JComboBox<>();
        entryAbstractionPanel = new org.jdesktop.swingx.JXPanel();
        entryAbstractionLabel = new javax.swing.JLabel();
        entryAbstractionLevelCombo = new javax.swing.JComboBox<>();
        entryQuestionPanel = new org.jdesktop.swingx.JXPanel();
        jLabel4 = new javax.swing.JLabel();
        entryQuestionCombo = new javax.swing.JComboBox<>();
        entryIaMarksPanel = new org.jdesktop.swingx.JXPanel();
        entryIaMarksLabel = new javax.swing.JLabel();
        entryIaMarksField = new javax.swing.JTextField();
        entryNewQuestionPanel = new org.jdesktop.swingx.JXPanel();
        entryQuestionLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        entryQuestionArea = new javax.swing.JTextArea();
        entryTotalMarksPanel = new org.jdesktop.swingx.JXPanel();
        entryTotalMarksLabel = new javax.swing.JLabel();
        entryTotalMarksField = new javax.swing.JTextField();
        entryCoPanel = new org.jdesktop.swingx.JXPanel();
        entryCoLabel = new javax.swing.JLabel();
        entryCoField = new javax.swing.JTextField();
        entryEnterButton = new javax.swing.JButton();
        entryTypeGroup = new javax.swing.ButtonGroup();
        invalidLoginOption = new javax.swing.JOptionPane();
        invalidRegistrationOption = new javax.swing.JOptionPane();
        validRegistrationOption = new javax.swing.JOptionPane();
        invalidUnregistrationOption = new javax.swing.JOptionPane();
        validUnregistrationOption = new javax.swing.JOptionPane();
        validEntryOption = new javax.swing.JOptionPane();
        invalidEntryOption = new javax.swing.JOptionPane();
        jLabel3 = new javax.swing.JLabel();
        unregisterTab = new javax.swing.JTabbedPane();
        viewLayer = new javax.swing.JLayeredPane();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        sectionPane = new javax.swing.JLayeredPane();
        sectionLabel = new javax.swing.JLabel();
        sectionCombo = new javax.swing.JComboBox<>();
        usnPane = new javax.swing.JLayeredPane();
        usnField = new javax.swing.JTextField();
        usnLabel = new javax.swing.JLabel();
        invalidUsnLabel = new javax.swing.JLabel();
        semesterPane = new javax.swing.JLayeredPane();
        semesterCombo = new javax.swing.JComboBox<>();
        semesterLabel = new javax.swing.JLabel();
        subjectPane = new javax.swing.JLayeredPane();
        subjectCombo = new javax.swing.JComboBox<>();
        subjectLabel = new javax.swing.JLabel();
        modulePane = new javax.swing.JLayeredPane();
        moduleCombo = new javax.swing.JComboBox<>();
        moduleLabel = new javax.swing.JLabel();
        difficultyPane = new javax.swing.JLayeredPane();
        difficultyLabel = new javax.swing.JLabel();
        difficultyCombo = new javax.swing.JComboBox<>();
        coPane = new javax.swing.JLayeredPane();
        coCombo = new javax.swing.JComboBox();
        coLabel = new javax.swing.JLabel();
        abstractionPane = new javax.swing.JLayeredPane();
        abstractionLevelCombo = new javax.swing.JComboBox();
        coLabel1 = new javax.swing.JLabel();
        datePane = new javax.swing.JLayeredPane();
        initialDateLabel = new javax.swing.JLabel();
        initialDateField = new datechooser.beans.DateChooserCombo();
        finalDateLabel = new javax.swing.JLabel();
        finalDateField = new datechooser.beans.DateChooserCombo();
        schedulePane = new javax.swing.JLayeredPane();
        initialDateLabel1 = new javax.swing.JLabel();
        finalDateLabel1 = new javax.swing.JLabel();
        initialDateField1 = new datechooser.beans.DateChooserCombo();
        finalDateField1 = new datechooser.beans.DateChooserCombo();
        dateRangeButton = new javax.swing.JButton();
        scheduleToggle = new javax.swing.JToggleButton();
        reportButton = new javax.swing.JButton();
        plotButton = new javax.swing.JButton();
        viewButton = new javax.swing.JButton();
        viewTabbedPane = new javax.swing.JTabbedPane();
        summaryLayer = new javax.swing.JLayeredPane();
        summaryCanvas = summaryCanvas;
        plotStatusBar = new org.jdesktop.swingx.JXStatusBar();
        legendLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        detailLayer = new javax.swing.JLayeredPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        detailTable = new org.jdesktop.swingx.JXTable();
        coStatusBar = new org.jdesktop.swingx.JXStatusBar();
        coStatusLabel = new javax.swing.JLabel();
        enterLayer = new javax.swing.JLayeredPane();
        loginPanel = new javax.swing.JPanel();
        loginUsnLabel = new javax.swing.JLabel();
        loginUSNField = new javax.swing.JTextField();
        loginPasswordLabel = new javax.swing.JLabel();
        loginPasswordField = new javax.swing.JPasswordField();
        loginButton = new javax.swing.JButton();
        registerLayer = new javax.swing.JLayeredPane();
        registerPanel = new javax.swing.JPanel();
        registerUSNLabel = new javax.swing.JLabel();
        registerUSNField = new javax.swing.JTextField();
        registerPasswordLabel = new javax.swing.JLabel();
        registerPasswordField = new javax.swing.JPasswordField();
        registerButton = new javax.swing.JButton();
        registerSectionLabel = new javax.swing.JLabel();
        registerSectionCombo = new javax.swing.JComboBox<>();
        unregisterLayer = new javax.swing.JLayeredPane();
        unregisterPanel = new javax.swing.JPanel();
        unregisterUsnLabel = new javax.swing.JLabel();
        unregisterUSNField = new javax.swing.JTextField();
        unregisterPasswordLabel = new javax.swing.JLabel();
        unregisterPasswordField = new javax.swing.JPasswordField();
        unregisterButton = new javax.swing.JButton();

        entryFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        entryFrame.setTitle("Subject Info Entry");
        entryFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        org.jdesktop.swingx.VerticalLayout verticalLayout4 = new org.jdesktop.swingx.VerticalLayout();
        verticalLayout4.setGap(15);
        jPanel1.setLayout(verticalLayout4);

        entryTypePanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
        entryTypePanel.setLayout(new org.jdesktop.swingx.HorizontalLayout());

        jLabel2.setText("Choose entry type:   ");
        entryTypePanel.add(jLabel2);

        adminStatusEntryRadio.setText("Admin Status Entry  ");
        adminStatusEntryRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminStatusEntryRadioActionPerformed(evt);
            }
        });
        entryTypePanel.add(adminStatusEntryRadio);
        adminStatusEntryRadio.addChangeListener(
            new ChangeListener(){
                @Override
                public void stateChanged(ChangeEvent e) {
                    if(adminStatusEntryRadio.isSelected()){
                        enableOnlyComponents(entryComponents,true,entryDifficultyPanel,entryDatePanel,entryTopicPanel,entryAbstractionPanel);
                        enterer.changeStrategy(new AdminStatusEntry());
                    }
                }
            }
        );

        statusEntryRadio.setText("Status Entry  ");
        entryTypePanel.add(statusEntryRadio);
        statusEntryRadio.addChangeListener(
            new ChangeListener(){
                @Override
                public void stateChanged(ChangeEvent e) {
                    if(statusEntryRadio.isSelected()){
                        enableOnlyComponents(entryComponents,true,entryDifficultyPanel,entryCoveredTopicPanel);
                        enterer.changeStrategy(new StatusEntry());
                    }
                }
            }
        );

        questionEntryRadio.setText("Question Entry  ");
        entryTypePanel.add(questionEntryRadio);
        questionEntryRadio.addChangeListener(
            new ChangeListener(){
                @Override
                public void stateChanged(ChangeEvent e) {
                    if(questionEntryRadio.isSelected()){
                        enableOnlyComponents(entryComponents,true,entryNewQuestionPanel,entryTotalMarksPanel,entryTopicPanel,entryCoPanel);
                        enterer.changeStrategy(new QuestionEntry());
                    }
                }
            }
        );

        syllabusEntryRadio.setText("Syllabus Entry  ");
        entryTypePanel.add(syllabusEntryRadio);
        syllabusEntryRadio.addChangeListener(
            new ChangeListener(){
                @Override
                public void stateChanged(ChangeEvent e) {
                    if(syllabusEntryRadio.isSelected()){
                        enableOnlyComponents(entryComponents,true,entryNewTopicPanel,entryModulePanel,entryPagePanel,entryTextbookPanel);
                        enterer.changeStrategy(new SyllabusEntry());
                    }
                }
            }
        );

        iaEntryRadio.setText("IA Marks Entry");
        entryTypePanel.add(iaEntryRadio);
        iaEntryRadio.addChangeListener(
            new ChangeListener(){
                @Override
                public void stateChanged(ChangeEvent e) {
                    if(iaEntryRadio.isSelected()){
                        enableOnlyComponents(entryComponents,true,entryQuestionPanel,entryIaMarksPanel,entryTotalMarksPanel);
                        enterer.changeStrategy(new IaEntry());
                    }
                }
            }
        );

        jPanel1.add(entryTypePanel);

        entrySemesterPanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
        entrySemesterPanel.setLayout(new java.awt.BorderLayout());

        entrySemesterLabel.setText("Choose semester (optional):");
        entrySemesterPanel.add(entrySemesterLabel, java.awt.BorderLayout.CENTER);

        entrySemesterCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "unselected", "5", "6" }));
        entrySemesterCombo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                entrySemesterComboPopupMenuWillBecomeVisible(evt);
            }
        });
        entrySemesterCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                entrySemesterComboActionPerformed(evt);
            }
        });
        entrySemesterPanel.add(entrySemesterCombo, java.awt.BorderLayout.PAGE_END);
        entrySemesterCombo.addItemListener(
            new ItemListener(){
                @Override
                public void itemStateChanged(ItemEvent e){
                    if(e.getStateChange()==ItemEvent.SELECTED){
                        entryViewer=entryViewer.withInputFilters(new SemesterFilter(getEntrySemester()));
                    }
                }
            });

            jPanel1.add(entrySemesterPanel);

            entryDatePanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
            entryDatePanel.setLayout(new java.awt.BorderLayout());

            entryDateLabel.setText("Enter date:");
            entryDatePanel.add(entryDateLabel, java.awt.BorderLayout.CENTER);

            entryDateField.setCurrentView(new datechooser.view.appearance.AppearancesList("Light",
                new datechooser.view.appearance.ViewAppearance("custom",
                    new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                        new java.awt.Color(0, 0, 0),
                        new java.awt.Color(0, 0, 255),
                        false,
                        true,
                        new datechooser.view.appearance.swing.ButtonPainter()),
                    new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                        new java.awt.Color(0, 0, 0),
                        new java.awt.Color(0, 0, 255),
                        true,
                        true,
                        new datechooser.view.appearance.swing.ButtonPainter()),
                    new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                        new java.awt.Color(0, 0, 255),
                        new java.awt.Color(0, 0, 255),
                        false,
                        true,
                        new datechooser.view.appearance.swing.ButtonPainter()),
                    new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                        new java.awt.Color(128, 128, 128),
                        new java.awt.Color(0, 0, 255),
                        false,
                        true,
                        new datechooser.view.appearance.swing.LabelPainter()),
                    new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                        new java.awt.Color(0, 0, 0),
                        new java.awt.Color(0, 0, 255),
                        false,
                        true,
                        new datechooser.view.appearance.swing.LabelPainter()),
                    new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                        new java.awt.Color(0, 0, 0),
                        new java.awt.Color(255, 0, 0),
                        false,
                        false,
                        new datechooser.view.appearance.swing.ButtonPainter()),
                    (datechooser.view.BackRenderer)null,
                    false,
                    true)));
        entryDatePanel.add(entryDateField, java.awt.BorderLayout.PAGE_END);
        entryDateField.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

        jPanel1.add(entryDatePanel);
        entryComponents.add(entryDatePanel);

        entrySubjectPanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
        entrySubjectPanel.setLayout(new java.awt.BorderLayout());

        entrySubjectLabel.setText("Choose subject name:");
        entrySubjectPanel.add(entrySubjectLabel, java.awt.BorderLayout.CENTER);

        entrySubjectCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "unselected" }));
        entrySubjectCombo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                entrySubjectComboPopupMenuWillBecomeVisible(evt);
            }
        });
        entrySubjectCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                entrySubjectComboActionPerformed(evt);
            }
        });
        entrySubjectPanel.add(entrySubjectCombo, java.awt.BorderLayout.PAGE_END);
        entrySubjectCombo.addItemListener(
            new ItemListener(){
                @Override
                public void itemStateChanged(ItemEvent e){
                    if(e.getStateChange()==ItemEvent.SELECTED){
                        entryViewer=entryViewer.withInputFilters(new SubjectFilter(getEntrySubject()));
                    }
                }
            });

            jPanel1.add(entrySubjectPanel);

            entryTopicPanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
            entryTopicPanel.setLayout(new java.awt.BorderLayout());

            entryTopicComboLabel.setText("Choose topic name:");
            entryTopicPanel.add(entryTopicComboLabel, java.awt.BorderLayout.CENTER);

            entryTopicCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "unselected" }));
            entryTopicCombo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                }
                public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                }
                public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                    entryTopicComboPopupMenuWillBecomeVisible(evt);
                }
            });
            entryTopicCombo.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    entryTopicComboActionPerformed(evt);
                }
            });
            entryTopicPanel.add(entryTopicCombo, java.awt.BorderLayout.PAGE_END);

            jPanel1.add(entryTopicPanel);
            entryComponents.add(entryTopicPanel);

            entryModulePanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
            entryModulePanel.setLayout(new java.awt.BorderLayout());

            entryModuleLabel.setText("Choose module number (optional):");
            entryModulePanel.add(entryModuleLabel, java.awt.BorderLayout.CENTER);

            entryModuleCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "unselected", "1", "2", "3", "4", "5" }));
            entryModuleCombo.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    entryModuleComboActionPerformed(evt);
                }
            });
            entryModulePanel.add(entryModuleCombo, java.awt.BorderLayout.PAGE_END);
            entryModuleCombo.addItemListener(
                new ItemListener(){
                    @Override
                    public void itemStateChanged(ItemEvent e){
                        if(e.getStateChange()==ItemEvent.SELECTED){
                            entryViewer=entryViewer.withInputFilters(new ModuleFilter(getEntryModule()));
                        }
                    }
                });

                jPanel1.add(entryModulePanel);

                entryCoveredTopicPanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
                entryCoveredTopicPanel.setLayout(new java.awt.BorderLayout());

                jLabel1.setText("Choose covered topics:");
                entryCoveredTopicPanel.add(jLabel1, java.awt.BorderLayout.CENTER);

                entryCoveredTopicCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "unselected" }));
                entryCoveredTopicCombo.addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent evt) {
                        entryCoveredTopicComboFocusGained(evt);
                    }
                });
                entryCoveredTopicCombo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                    public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                    }
                    public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                    }
                    public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                        entryCoveredTopicComboPopupMenuWillBecomeVisible(evt);
                    }
                });
                entryCoveredTopicCombo.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        entryCoveredTopicComboActionPerformed(evt);
                    }
                });
                entryCoveredTopicPanel.add(entryCoveredTopicCombo, java.awt.BorderLayout.PAGE_END);

                jPanel1.add(entryCoveredTopicPanel);
                entryComponents.add(entryCoveredTopicPanel);

                entryNewTopicPanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
                entryNewTopicPanel.setLayout(new java.awt.BorderLayout());

                entryTopicFieldLabel.setText("Enter topic name:");
                entryNewTopicPanel.add(entryTopicFieldLabel, java.awt.BorderLayout.CENTER);
                entryNewTopicPanel.add(entryTopicField, java.awt.BorderLayout.PAGE_END);

                jPanel1.add(entryNewTopicPanel);
                entryComponents.add(entryNewTopicPanel);

                entryTextbookPanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
                entryTextbookPanel.setLayout(new java.awt.BorderLayout());

                entryTextbookLabel.setText("Choose textbook name:");
                entryTextbookPanel.add(entryTextbookLabel, java.awt.BorderLayout.CENTER);

                entryTextbookCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "unselected" }));
                entryTextbookCombo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                    public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                    }
                    public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                    }
                    public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                        entryTextbookComboPopupMenuWillBecomeVisible(evt);
                    }
                });
                entryTextbookPanel.add(entryTextbookCombo, java.awt.BorderLayout.PAGE_END);

                jPanel1.add(entryTextbookPanel);
                entryComponents.add(entryTextbookPanel);

                entryPagePanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
                entryPagePanel.setLayout(new java.awt.BorderLayout());

                entryPageLabel.setText("Enter page number:");
                entryPagePanel.add(entryPageLabel, java.awt.BorderLayout.CENTER);

                entryPageField.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        entryPageFieldActionPerformed(evt);
                    }
                });
                entryPagePanel.add(entryPageField, java.awt.BorderLayout.PAGE_END);

                jPanel1.add(entryPagePanel);
                entryComponents.add(entryPagePanel);

                entryDifficultyPanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
                entryDifficultyPanel.setLayout(new java.awt.BorderLayout());

                entryDifficultyLabel.setText("Choose difficulty level:");
                entryDifficultyPanel.add(entryDifficultyLabel, java.awt.BorderLayout.CENTER);

                entryDifficultyCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "unselected", "Hard", "Medium", "Easy" }));
                entryDifficultyPanel.add(entryDifficultyCombo, java.awt.BorderLayout.PAGE_END);

                jPanel1.add(entryDifficultyPanel);
                entryComponents.add(entryDifficultyPanel);

                entryAbstractionPanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
                entryAbstractionPanel.setLayout(new java.awt.BorderLayout());

                entryAbstractionLabel.setText("Choose abstraction level:");
                entryAbstractionPanel.add(entryAbstractionLabel, java.awt.BorderLayout.CENTER);

                entryAbstractionLevelCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "unselected", "1", "1.1", "1.11", "1.12", "1.13", "1.2", "1.21", "1.22", "1.3", "1.31", "1.32", "1.4", "1.41", "1.42", "1.43", "1.44", "1.5", "1.51", "1.52", "2", "2.1", "2.11", "2.2", "2.22", "2.3" }));
                entryAbstractionPanel.add(entryAbstractionLevelCombo, java.awt.BorderLayout.PAGE_END);

                jPanel1.add(entryAbstractionPanel);
                entryComponents.add(entryAbstractionPanel);

                entryQuestionPanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
                entryQuestionPanel.setLayout(new java.awt.BorderLayout());

                jLabel4.setText("Choose question:");
                entryQuestionPanel.add(jLabel4, java.awt.BorderLayout.CENTER);

                entryQuestionCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "unselected" }));
                entryQuestionCombo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                    public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                    }
                    public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                    }
                    public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                        entryQuestionComboPopupMenuWillBecomeVisible(evt);
                    }
                });
                entryQuestionPanel.add(entryQuestionCombo, java.awt.BorderLayout.PAGE_END);

                jPanel1.add(entryQuestionPanel);
                entryComponents.add(entryQuestionPanel);

                entryIaMarksPanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
                entryIaMarksPanel.setLayout(new java.awt.BorderLayout());

                entryIaMarksLabel.setText("Enter IA marks:");
                entryIaMarksPanel.add(entryIaMarksLabel, java.awt.BorderLayout.CENTER);
                entryIaMarksPanel.add(entryIaMarksField, java.awt.BorderLayout.PAGE_END);

                jPanel1.add(entryIaMarksPanel);
                entryComponents.add(entryIaMarksPanel);

                entryNewQuestionPanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
                entryNewQuestionPanel.setLayout(new java.awt.BorderLayout());

                entryQuestionLabel.setText("Enter question:");
                entryNewQuestionPanel.add(entryQuestionLabel, java.awt.BorderLayout.CENTER);

                entryQuestionArea.setColumns(20);
                entryQuestionArea.setRows(5);
                jScrollPane3.setViewportView(entryQuestionArea);

                entryNewQuestionPanel.add(jScrollPane3, java.awt.BorderLayout.PAGE_END);

                jPanel1.add(entryNewQuestionPanel);
                entryComponents.add(entryNewQuestionPanel);

                entryTotalMarksPanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
                entryTotalMarksPanel.setLayout(new java.awt.BorderLayout());

                entryTotalMarksLabel.setText("Enter total marks:");
                entryTotalMarksPanel.add(entryTotalMarksLabel, java.awt.BorderLayout.CENTER);
                entryTotalMarksPanel.add(entryTotalMarksField, java.awt.BorderLayout.PAGE_END);

                jPanel1.add(entryTotalMarksPanel);
                entryComponents.add(entryTotalMarksPanel);

                entryCoPanel.setBorder(new org.jdesktop.swingx.border.DropShadowBorder());
                entryCoPanel.setLayout(new java.awt.BorderLayout());

                entryCoLabel.setText("Enter CO:");
                entryCoPanel.add(entryCoLabel, java.awt.BorderLayout.CENTER);
                entryCoPanel.add(entryCoField, java.awt.BorderLayout.PAGE_END);

                jPanel1.add(entryCoPanel);
                entryComponents.add(entryCoPanel);

                entryEnterButton.setText("Enter");
                entryEnterButton.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        entryEnterButtonMouseClicked(evt);
                    }
                });
                entryEnterButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        entryEnterButtonActionPerformed(evt);
                    }
                });
                jPanel1.add(entryEnterButton);
                entryEnterButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                jScrollPane1.setViewportView(jPanel1);

                entryFrame.getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

                entryTypeGroup.add(statusEntryRadio);
                entryTypeGroup.add(adminStatusEntryRadio);
                entryTypeGroup.add(iaEntryRadio);
                entryTypeGroup.add(questionEntryRadio);
                entryTypeGroup.add(syllabusEntryRadio);

                invalidLoginOption.setMessage("usn or password is incorrect");

                invalidRegistrationOption.setMessage("student already exists or incorrect registration details");

                validRegistrationOption.setMessage("registration successful");

                invalidUnregistrationOption.setMessage("no such student exists");

                validUnregistrationOption.setMessage("unregistration successful");

                validEntryOption.setMessage("successfully entered");

                invalidEntryOption.setMessage("fill in all the necessary fields");

                jLabel3.setText("jLabel3");

                setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
                setTitle("SSMS- Student Semester Management System");

                unregisterTab.setAutoscrolls(true);
                unregisterTab.setMaximumSize(new java.awt.Dimension(42767, 32767));
                unregisterTab.setName(""); // NOI18N
                unregisterTab.setPreferredSize(getPreferredSize());

                viewLayer.setPreferredSize(new java.awt.Dimension(1185, 494));
                viewLayer.setLayout(new java.awt.BorderLayout());

                jLayeredPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filter By:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Century Gothic", 0, 12))); // NOI18N
                jLayeredPane1.setPreferredSize(new java.awt.Dimension(1433, 190));
                jLayeredPane1.setRequestFocusEnabled(false);
                jLayeredPane1.setLayout(new java.awt.FlowLayout());

                sectionLabel.setText("  Section  :");

                sectionCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "unselected", "A", "B", "C" }));

                sectionPane.setLayer(sectionLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
                sectionPane.setLayer(sectionCombo, javax.swing.JLayeredPane.DEFAULT_LAYER);

                javax.swing.GroupLayout sectionPaneLayout = new javax.swing.GroupLayout(sectionPane);
                sectionPane.setLayout(sectionPaneLayout);
                sectionPaneLayout.setHorizontalGroup(
                    sectionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sectionPaneLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(sectionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(6, 6, 6)
                        .addComponent(sectionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                );
                sectionPaneLayout.setVerticalGroup(
                    sectionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sectionPaneLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(sectionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(sectionLabel)
                            .addComponent(sectionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
                );

                sectionCombo.addItemListener(
                    new ItemListener(){
                        @Override
                        public void itemStateChanged(ItemEvent e){
                            if(e.getStateChange()==ItemEvent.SELECTED){
                                if(!CheckHelper.checkEmpty(getSection()))
                                usnField.setEnabled(false);
                                else
                                usnField.setEnabled(true);

                                viewer=viewer.withInputBase(new SectionTableQuery(getSection()),new DefaultCoveredBase());
                                viewer=viewer.withInputFilters(new SectionFilter(getSection()));
                                viewButton.doClick();
                                plotButton.doClick();
                            }
                        }
                    });

                    jLayeredPane1.add(sectionPane);
                    viewingComponents.add(sectionPane);

                    usnField.setToolTipText("1hkXXcsXXX");
                    usnField.setMaximumSize(new java.awt.Dimension(6, 20));
                    usnField.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                            usnFieldMouseClicked(evt);
                        }
                        public void mouseEntered(java.awt.event.MouseEvent evt) {
                            usnFieldMouseEntered(evt);
                        }
                    });
                    usnField.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            usnFieldActionPerformed(evt);
                        }
                    });

                    usnLabel.setText("USN  :");

                    invalidUsnLabel.setForeground(new java.awt.Color(255, 0, 0));
                    invalidUsnLabel.setText("usn doesn't exist");

                    usnPane.setLayer(usnField, javax.swing.JLayeredPane.DEFAULT_LAYER);
                    usnPane.setLayer(usnLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
                    usnPane.setLayer(invalidUsnLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);

                    javax.swing.GroupLayout usnPaneLayout = new javax.swing.GroupLayout(usnPane);
                    usnPane.setLayout(usnPaneLayout);
                    usnPaneLayout.setHorizontalGroup(
                        usnPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, usnPaneLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(usnLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(usnPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(invalidUsnLabel)
                                .addComponent(usnField, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addContainerGap())
                    );
                    usnPaneLayout.setVerticalGroup(
                        usnPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(usnPaneLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(usnPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                .addComponent(usnField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(usnLabel))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(invalidUsnLabel)
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    );

                    usnField.getDocument().addDocumentListener(new DocumentListener() {
                        public void changedUpdate(DocumentEvent e) {
                            changed();
                        }
                        public void removeUpdate(DocumentEvent e) {
                            changed();
                        }
                        public void insertUpdate(DocumentEvent e) {
                            changed();
                        }

                        public void changed() {
                            if (getUSN().trim().equals("")){
                                invalidUsnLabel.setVisible(false);
                                sectionCombo.setEnabled(true);
                                viewer=viewer.withBase(new DefaultCoveredBase());
                                viewer=viewer.withoutFilters(new USNFilter(getUSN()));
                                viewButton.doClick();
                                plotButton.doClick();
                            }
                            else if(getUSN().trim().length()>=10) {
                                sectionCombo.setEnabled(false);
                                viewer=viewer.withBase(new StudentTableQuery(getUSN()));
                                viewer=viewer.withFilters(new USNFilter(getUSN()));
                                viewButton.doClick();
                                plotButton.doClick();
                                new SwingWorker<Boolean,Void>(){
                                    protected Boolean doInBackground(){
                                        return !gateway.isExistingUser(getUSN()) && !CheckHelper.checkEmpty(getUSN());
                                    }
                                    protected void done(){
                                        try{
                                            if(get())
                                            invalidUsnLabel.setVisible(true);
                                            else
                                            invalidUsnLabel.setVisible(false);
                                        }
                                        catch (InterruptedException | ExecutionException ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }.execute();
                            }
                        }
                    });
                    invalidUsnLabel.setVisible(false);

                    jLayeredPane1.add(usnPane);
                    viewingComponents.add(usnPane);

                    semesterCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "unselected", "5", "6" }));

                    semesterLabel.setText("Semester :");

                    semesterPane.setLayer(semesterCombo, javax.swing.JLayeredPane.DEFAULT_LAYER);
                    semesterPane.setLayer(semesterLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);

                    javax.swing.GroupLayout semesterPaneLayout = new javax.swing.GroupLayout(semesterPane);
                    semesterPane.setLayout(semesterPaneLayout);
                    semesterPaneLayout.setHorizontalGroup(
                        semesterPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(semesterPaneLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(semesterLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(semesterCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                    );
                    semesterPaneLayout.setVerticalGroup(
                        semesterPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(semesterPaneLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(semesterPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                .addComponent(semesterLabel)
                                .addComponent(semesterCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addContainerGap())
                    );

                    semesterCombo.addItemListener(
                        new ItemListener(){
                            @Override
                            public void itemStateChanged(ItemEvent e){
                                if(e.getStateChange()==ItemEvent.SELECTED){
                                    viewer=viewer.withInputFilters(new SemesterFilter(getSemester()));
                                    //refreshTable();
                                    viewButton.doClick();
                                }
                            }
                        });

                        jLayeredPane1.add(semesterPane);
                        viewingComponents.add(semesterPane);

                        subjectCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "unselected" }));
                        subjectCombo.addItemListener(new java.awt.event.ItemListener() {
                            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                                subjectComboItemStateChanged(evt);
                            }
                        });
                        subjectCombo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                            }
                            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                            }
                            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                                subjectComboPopupMenuWillBecomeVisible(evt);
                            }
                        });
                        subjectCombo.addActionListener(new java.awt.event.ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                subjectComboActionPerformed(evt);
                            }
                        });

                        subjectLabel.setText("  Subject  :");

                        subjectPane.setLayer(subjectCombo, javax.swing.JLayeredPane.DEFAULT_LAYER);
                        subjectPane.setLayer(subjectLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);

                        javax.swing.GroupLayout subjectPaneLayout = new javax.swing.GroupLayout(subjectPane);
                        subjectPane.setLayout(subjectPaneLayout);
                        subjectPaneLayout.setHorizontalGroup(
                            subjectPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(subjectPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(subjectLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(subjectCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                        );
                        subjectPaneLayout.setVerticalGroup(
                            subjectPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(subjectPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(subjectPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                    .addComponent(subjectLabel)
                                    .addComponent(subjectCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
                        );

                        jLayeredPane1.add(subjectPane);
                        viewingComponents.add(subjectPane);

                        moduleCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "unselected", "1", "2", "3", "4", "5" }));

                        moduleLabel.setText("  Module  :");

                        modulePane.setLayer(moduleCombo, javax.swing.JLayeredPane.DEFAULT_LAYER);
                        modulePane.setLayer(moduleLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);

                        javax.swing.GroupLayout modulePaneLayout = new javax.swing.GroupLayout(modulePane);
                        modulePane.setLayout(modulePaneLayout);
                        modulePaneLayout.setHorizontalGroup(
                            modulePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(modulePaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(moduleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(moduleCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                        );
                        modulePaneLayout.setVerticalGroup(
                            modulePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(modulePaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(modulePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                    .addComponent(moduleLabel)
                                    .addComponent(moduleCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
                        );

                        moduleCombo.addItemListener(
                            new ItemListener(){
                                @Override
                                public void itemStateChanged(ItemEvent e){
                                    if(e.getStateChange()==ItemEvent.SELECTED){
                                        viewer=viewer.withInputFilters(new ModuleFilter(getModule()));
                                        //refreshTable();
                                        viewButton.doClick();
                                        plotButton.doClick();
                                    }
                                }
                            });

                            jLayeredPane1.add(modulePane);
                            viewingComponents.add(modulePane);

                            difficultyLabel.setText("Difficulty  :");

                            difficultyCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "unselected", "Hard", "Medium", "Easy" }));

                            difficultyPane.setLayer(difficultyLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
                            difficultyPane.setLayer(difficultyCombo, javax.swing.JLayeredPane.DEFAULT_LAYER);

                            javax.swing.GroupLayout difficultyPaneLayout = new javax.swing.GroupLayout(difficultyPane);
                            difficultyPane.setLayout(difficultyPaneLayout);
                            difficultyPaneLayout.setHorizontalGroup(
                                difficultyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(difficultyPaneLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(difficultyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(difficultyCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap())
                            );
                            difficultyPaneLayout.setVerticalGroup(
                                difficultyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(difficultyPaneLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(difficultyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(difficultyLabel)
                                        .addComponent(difficultyCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addContainerGap())
                            );

                            difficultyCombo.addItemListener(
                                new ItemListener(){
                                    @Override
                                    public void itemStateChanged(ItemEvent e){
                                        if(e.getStateChange()==ItemEvent.SELECTED){

                                            viewer=viewer.withInputFilters(new DifficultyFilter(getDifficulty()));
                                            //refreshTable();
                                            viewButton.doClick();
                                        }
                                    }
                                });

                                jLayeredPane1.add(difficultyPane);
                                viewingComponents.add(difficultyPane);

                                coCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "unselected", "1", "2", "3", "4", "5" }));

                                coLabel.setText("CO: ");

                                coPane.setLayer(coCombo, javax.swing.JLayeredPane.DEFAULT_LAYER);
                                coPane.setLayer(coLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);

                                javax.swing.GroupLayout coPaneLayout = new javax.swing.GroupLayout(coPane);
                                coPane.setLayout(coPaneLayout);
                                coPaneLayout.setHorizontalGroup(
                                    coPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(coPaneLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(coLabel)
                                        .addGap(5, 5, 5)
                                        .addComponent(coCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap())
                                );
                                coPaneLayout.setVerticalGroup(
                                    coPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(coPaneLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(coPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                            .addComponent(coCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(coLabel))
                                        .addContainerGap())
                                );

                                coCombo.addItemListener(
                                    new ItemListener(){
                                        @Override
                                        public void itemStateChanged(ItemEvent e){
                                            if(e.getStateChange()==ItemEvent.SELECTED){

                                                viewer=viewer.withInputFilters(new COFilter(getCO()));
                                                //refreshTable();

                                                viewButton.doClick();
                                                plotButton.doClick();
                                                //                    if(!CheckHelper.checkEmpty(getUSN(),getCO(),getSubject()) && !invalidUsnLabel.isVisible()){
                                                    //                        coStatusLabel.setText("CO "+(String)coCombo.getSelectedItem()+" average: "+gateway.getCOAvg(getSubject(),getCO(),getUSN()));
                                                    //                    }
                                                //                    else
                                                //                        coStatusLabel.setText("");
                                            }
                                        }
                                    });

                                    jLayeredPane1.add(coPane);
                                    viewingComponents.add(coPane);

                                    abstractionLevelCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "unselected", "1", "1.1", "1.11", "1.12", "1.13", "1.2", "1.21", "1.22", "1.3", "1.31", "1.32", "1.4", "1.41", "1.42", "1.43", "1.44", "1.5", "1.51", "1.52", "2", "2.1", "2.11", "2.2", "2.22", "2.3" }));

                                    coLabel1.setText("Abstraction level: ");

                                    abstractionPane.setLayer(abstractionLevelCombo, javax.swing.JLayeredPane.DEFAULT_LAYER);
                                    abstractionPane.setLayer(coLabel1, javax.swing.JLayeredPane.DEFAULT_LAYER);

                                    javax.swing.GroupLayout abstractionPaneLayout = new javax.swing.GroupLayout(abstractionPane);
                                    abstractionPane.setLayout(abstractionPaneLayout);
                                    abstractionPaneLayout.setHorizontalGroup(
                                        abstractionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(abstractionPaneLayout.createSequentialGroup()
                                            .addContainerGap()
                                            .addComponent(coLabel1)
                                            .addGap(5, 5, 5)
                                            .addComponent(abstractionLevelCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addContainerGap())
                                    );
                                    abstractionPaneLayout.setVerticalGroup(
                                        abstractionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(abstractionPaneLayout.createSequentialGroup()
                                            .addContainerGap()
                                            .addGroup(abstractionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                                .addComponent(abstractionLevelCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(coLabel1))
                                            .addContainerGap())
                                    );

                                    abstractionLevelCombo.addItemListener(
                                        new ItemListener(){
                                            @Override
                                            public void itemStateChanged(ItemEvent e){
                                                if(e.getStateChange()==ItemEvent.SELECTED){
                                                    viewer=viewer.withInputFilters(new AbstractionFilter(getAbstractionLevel()));
                                                    viewButton.doClick();
                                                }
                                            }
                                        });

                                        jLayeredPane1.add(abstractionPane);
                                        viewingComponents.add(abstractionPane);

                                        initialDateLabel.setText("  Initial date  :");

                                        initialDateField.setCurrentView(new datechooser.view.appearance.AppearancesList("Light",
                                            new datechooser.view.appearance.ViewAppearance("custom",
                                                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                    new java.awt.Color(0, 0, 0),
                                                    new java.awt.Color(0, 0, 255),
                                                    false,
                                                    true,
                                                    new datechooser.view.appearance.swing.ButtonPainter()),
                                                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                    new java.awt.Color(0, 0, 0),
                                                    new java.awt.Color(0, 0, 255),
                                                    true,
                                                    true,
                                                    new datechooser.view.appearance.swing.ButtonPainter()),
                                                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                    new java.awt.Color(0, 0, 255),
                                                    new java.awt.Color(0, 0, 255),
                                                    false,
                                                    true,
                                                    new datechooser.view.appearance.swing.ButtonPainter()),
                                                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                    new java.awt.Color(128, 128, 128),
                                                    new java.awt.Color(0, 0, 255),
                                                    false,
                                                    true,
                                                    new datechooser.view.appearance.swing.LabelPainter()),
                                                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                    new java.awt.Color(0, 0, 0),
                                                    new java.awt.Color(0, 0, 255),
                                                    false,
                                                    true,
                                                    new datechooser.view.appearance.swing.LabelPainter()),
                                                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                    new java.awt.Color(0, 0, 0),
                                                    new java.awt.Color(255, 0, 0),
                                                    false,
                                                    false,
                                                    new datechooser.view.appearance.swing.ButtonPainter()),
                                                (datechooser.view.BackRenderer)null,
                                                false,
                                                true)));
                                    try {
                                        initialDateField.setDefaultPeriods(new datechooser.model.multiple.PeriodSet());
                                    } catch (datechooser.model.exeptions.IncompatibleDataExeption e1) {
                                        e1.printStackTrace();
                                    }

                                    finalDateLabel.setText("  Final date  :");

                                    finalDateField.setCurrentView(new datechooser.view.appearance.AppearancesList("Light",
                                        new datechooser.view.appearance.ViewAppearance("custom",
                                            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                new java.awt.Color(0, 0, 0),
                                                new java.awt.Color(0, 0, 255),
                                                false,
                                                true,
                                                new datechooser.view.appearance.swing.ButtonPainter()),
                                            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                new java.awt.Color(0, 0, 0),
                                                new java.awt.Color(0, 0, 255),
                                                true,
                                                true,
                                                new datechooser.view.appearance.swing.ButtonPainter()),
                                            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                new java.awt.Color(0, 0, 255),
                                                new java.awt.Color(0, 0, 255),
                                                false,
                                                true,
                                                new datechooser.view.appearance.swing.ButtonPainter()),
                                            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                new java.awt.Color(128, 128, 128),
                                                new java.awt.Color(0, 0, 255),
                                                false,
                                                true,
                                                new datechooser.view.appearance.swing.LabelPainter()),
                                            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                new java.awt.Color(0, 0, 0),
                                                new java.awt.Color(0, 0, 255),
                                                false,
                                                true,
                                                new datechooser.view.appearance.swing.LabelPainter()),
                                            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                new java.awt.Color(0, 0, 0),
                                                new java.awt.Color(255, 0, 0),
                                                false,
                                                false,
                                                new datechooser.view.appearance.swing.ButtonPainter()),
                                            (datechooser.view.BackRenderer)null,
                                            false,
                                            true)));
                                try {
                                    finalDateField.setDefaultPeriods(new datechooser.model.multiple.PeriodSet());
                                } catch (datechooser.model.exeptions.IncompatibleDataExeption e1) {
                                    e1.printStackTrace();
                                }

                                datePane.setLayer(initialDateLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
                                datePane.setLayer(initialDateField, javax.swing.JLayeredPane.DEFAULT_LAYER);
                                datePane.setLayer(finalDateLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
                                datePane.setLayer(finalDateField, javax.swing.JLayeredPane.DEFAULT_LAYER);

                                javax.swing.GroupLayout datePaneLayout = new javax.swing.GroupLayout(datePane);
                                datePane.setLayout(datePaneLayout);
                                datePaneLayout.setHorizontalGroup(
                                    datePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(datePaneLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(initialDateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(initialDateField, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(finalDateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(finalDateField, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap())
                                );
                                datePaneLayout.setVerticalGroup(
                                    datePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(datePaneLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(datePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                            .addComponent(initialDateLabel)
                                            .addComponent(finalDateLabel)
                                            .addComponent(finalDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(initialDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addContainerGap())
                                );

                                initialDateField.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
                                initialDateField.addCommitListener(
                                    new CommitListener(){
                                        @Override
                                        public void onCommit(CommitEvent e){
                                            //if(distributionCombo.getSelectedIndex()==0)
                                            viewer=viewer.withInputFilters(new DateFilter(getInitialDate(),getFinalDate()));
                                            //refreshTable();
                                            viewButton.doClick();
                                            plotButton.doClick();
                                        }
                                    });
                                    finalDateField.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
                                    finalDateField.addCommitListener(
                                        new CommitListener(){
                                            @Override
                                            public void onCommit(CommitEvent e){
                                                //if(distributionCombo.getSelectedIndex()==0)
                                                viewer=viewer.withInputFilters(new DateFilter(getInitialDate(),getFinalDate()));
                                                //refreshTable();
                                                viewButton.doClick();
                                                plotButton.doClick();
                                                //if(!CheckHelper.checkEmpty(getInitialDate(),getFinalDate()))
                                                //    dateRangeButton.setEnabled(true);
                                                //else
                                                //    dateRangeButton.setEnabled(false);
                                            }
                                        });

                                        jLayeredPane1.add(datePane);
                                        viewingComponents.add(datePane);

                                        schedulePane.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, null, null, null, java.awt.Color.lightGray), "Schedule selection:", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Century Gothic", 0, 11))); // NOI18N

                                        initialDateLabel1.setText("  Initial date  :");

                                        finalDateLabel1.setText("  Final date  :");

                                        initialDateField1.setCurrentView(new datechooser.view.appearance.AppearancesList("Light",
                                            new datechooser.view.appearance.ViewAppearance("custom",
                                                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                    new java.awt.Color(0, 0, 0),
                                                    new java.awt.Color(0, 0, 255),
                                                    false,
                                                    true,
                                                    new datechooser.view.appearance.swing.ButtonPainter()),
                                                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                    new java.awt.Color(0, 0, 0),
                                                    new java.awt.Color(0, 0, 255),
                                                    true,
                                                    true,
                                                    new datechooser.view.appearance.swing.ButtonPainter()),
                                                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                    new java.awt.Color(0, 0, 255),
                                                    new java.awt.Color(0, 0, 255),
                                                    false,
                                                    true,
                                                    new datechooser.view.appearance.swing.ButtonPainter()),
                                                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                    new java.awt.Color(128, 128, 128),
                                                    new java.awt.Color(0, 0, 255),
                                                    false,
                                                    true,
                                                    new datechooser.view.appearance.swing.LabelPainter()),
                                                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                    new java.awt.Color(0, 0, 0),
                                                    new java.awt.Color(0, 0, 255),
                                                    false,
                                                    true,
                                                    new datechooser.view.appearance.swing.LabelPainter()),
                                                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                    new java.awt.Color(0, 0, 0),
                                                    new java.awt.Color(255, 0, 0),
                                                    false,
                                                    false,
                                                    new datechooser.view.appearance.swing.ButtonPainter()),
                                                (datechooser.view.BackRenderer)null,
                                                false,
                                                true)));

                                    finalDateField1.setCurrentView(new datechooser.view.appearance.AppearancesList("Light",
                                        new datechooser.view.appearance.ViewAppearance("custom",
                                            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                new java.awt.Color(0, 0, 0),
                                                new java.awt.Color(0, 0, 255),
                                                false,
                                                true,
                                                new datechooser.view.appearance.swing.ButtonPainter()),
                                            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                new java.awt.Color(0, 0, 0),
                                                new java.awt.Color(0, 0, 255),
                                                true,
                                                true,
                                                new datechooser.view.appearance.swing.ButtonPainter()),
                                            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                new java.awt.Color(0, 0, 255),
                                                new java.awt.Color(0, 0, 255),
                                                false,
                                                true,
                                                new datechooser.view.appearance.swing.ButtonPainter()),
                                            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                new java.awt.Color(128, 128, 128),
                                                new java.awt.Color(0, 0, 255),
                                                false,
                                                true,
                                                new datechooser.view.appearance.swing.LabelPainter()),
                                            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                new java.awt.Color(0, 0, 0),
                                                new java.awt.Color(0, 0, 255),
                                                false,
                                                true,
                                                new datechooser.view.appearance.swing.LabelPainter()),
                                            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                                                new java.awt.Color(0, 0, 0),
                                                new java.awt.Color(255, 0, 0),
                                                false,
                                                false,
                                                new datechooser.view.appearance.swing.ButtonPainter()),
                                            (datechooser.view.BackRenderer)null,
                                            false,
                                            true)));
                                try {
                                    finalDateField1.setDefaultPeriods(new datechooser.model.multiple.PeriodSet());
                                } catch (datechooser.model.exeptions.IncompatibleDataExeption e1) {
                                    e1.printStackTrace();
                                }

                                dateRangeButton.setText("select");
                                dateRangeButton.setEnabled(false);
                                dateRangeButton.addActionListener(new java.awt.event.ActionListener() {
                                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                                        dateRangeButtonActionPerformed(evt);
                                    }
                                });

                                schedulePane.setLayer(initialDateLabel1, javax.swing.JLayeredPane.DEFAULT_LAYER);
                                schedulePane.setLayer(finalDateLabel1, javax.swing.JLayeredPane.DEFAULT_LAYER);
                                schedulePane.setLayer(initialDateField1, javax.swing.JLayeredPane.DEFAULT_LAYER);
                                schedulePane.setLayer(finalDateField1, javax.swing.JLayeredPane.DEFAULT_LAYER);
                                schedulePane.setLayer(dateRangeButton, javax.swing.JLayeredPane.DEFAULT_LAYER);

                                javax.swing.GroupLayout schedulePaneLayout = new javax.swing.GroupLayout(schedulePane);
                                schedulePane.setLayout(schedulePaneLayout);
                                schedulePaneLayout.setHorizontalGroup(
                                    schedulePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(schedulePaneLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(initialDateLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(initialDateField1, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(finalDateLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(finalDateField1, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(dateRangeButton)
                                        .addContainerGap())
                                );
                                schedulePaneLayout.setVerticalGroup(
                                    schedulePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(schedulePaneLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(schedulePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                            .addComponent(initialDateLabel1)
                                            .addComponent(finalDateLabel1)
                                            .addComponent(initialDateField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(finalDateField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(dateRangeButton))
                                        .addContainerGap())
                                );

                                initialDateField1.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
                                initialDateField1.addCommitListener(
                                    new CommitListener(){
                                        @Override
                                        public void onCommit(CommitEvent e){
                                            //                    if(distributionCombo.getSelectedIndex()==0)
                                            //configureFilter(viewer,new DateFilter(getInitialDate(),getFinalDate()),getInitialDate());
                                            //refreshTable();
                                            //viewButton.doClick();
                                            if(!CheckHelper.checkEmpty(initialDateField1.getText(),finalDateField1.getText()))
                                            dateRangeButton.setEnabled(true);
                                            else
                                            dateRangeButton.setEnabled(false);
                                        }
                                    });
                                    finalDateField1.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
                                    finalDateField1.addCommitListener(
                                        new CommitListener(){
                                            @Override
                                            public void onCommit(CommitEvent e){
                                                //                    if(distributionCombo.getSelectedIndex()==0)
                                                //configureFilter(viewer,new DateFilter(getInitialDate(),getFinalDate()),getFinalDate());
                                                //refreshTable();
                                                //viewButton.doClick();
                                                if(!CheckHelper.checkEmpty(initialDateField1.getText(),finalDateField1.getText()))
                                                dateRangeButton.setEnabled(true);
                                                else
                                                dateRangeButton.setEnabled(false);
                                            }
                                        });

                                        jLayeredPane1.add(schedulePane);
                                        schedulePane.setVisible(false);
                                        viewingComponents.add(schedulePane);

                                        scheduleToggle.setText("Schedule");
                                        scheduleToggle.addActionListener(new java.awt.event.ActionListener() {
                                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                                scheduleToggleActionPerformed(evt);
                                            }
                                        });
                                        jLayeredPane1.add(scheduleToggle);
                                        viewingComponents.add(scheduleToggle);

                                        reportButton.setText("Generate report");
                                        reportButton.addActionListener(new java.awt.event.ActionListener() {
                                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                                reportButtonActionPerformed(evt);
                                            }
                                        });
                                        jLayeredPane1.add(reportButton);
                                        viewingComponents.add(reportButton);

                                        plotButton.setText("Plot");
                                        plotButton.addActionListener(new java.awt.event.ActionListener() {
                                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                                plotButtonActionPerformed(evt);
                                            }
                                        });
                                        jLayeredPane1.add(plotButton);
                                        viewingComponents.add(plotButton);
                                        plotButton.doClick();

                                        viewButton.setText("View");
                                        viewButton.addActionListener(new java.awt.event.ActionListener() {
                                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                                viewButtonActionPerformed(evt);
                                            }
                                        });
                                        jLayeredPane1.add(viewButton);
                                        viewingComponents.add(viewButton);

                                        viewLayer.add(jLayeredPane1, java.awt.BorderLayout.PAGE_START);

                                        viewTabbedPane.addComponentListener(new java.awt.event.ComponentAdapter() {
                                            public void componentShown(java.awt.event.ComponentEvent evt) {
                                                viewTabbedPaneComponentShown(evt);
                                            }
                                        });

                                        summaryLayer.setLayout(new java.awt.BorderLayout());

                                        summaryLayer.add(summaryCanvas, java.awt.BorderLayout.CENTER);
                                        summaryCanvas.addGLEventListener(render);
                                        FPSAnimator animator=new FPSAnimator(summaryCanvas,300,true);
                                        animator.start();

                                        legendLabel.setText("Reddish: Medium | Blueish: Easy | Blackish: Difficult");
                                        plotStatusBar.add(legendLabel);

                                        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                                        jLabel6.setText("XAxis: Semesters | YAxis: Abstraction levels");
                                        plotStatusBar.add(jLabel6);

                                        summaryLayer.add(plotStatusBar, java.awt.BorderLayout.PAGE_END);

                                        viewTabbedPane.addTab("View summary", summaryLayer);

                                        detailLayer.setLayout(new java.awt.BorderLayout());

                                        detailTable.setModel(detailTableModel);
                                        detailTable.addMouseListener(new MouseAdapter(){
                                            public void mouseReleased(MouseEvent me){
                                                if(me.getClickCount()==1 && detailTable.getSelectedColumn()==1){
                                                    String textbookName=(String)detailTable.getValueAt(detailTable.getSelectedRow(),1);
                                                    String link=gateway.getLink(textbookName);
                                                    if(link!=null)
                                                    NETHelper.openWebpage(link);
                                                }
                                            }
                                        });
                                        //hide col 5

                                        //viewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                                        detailTable.setSortable(true);
                                        detailTable.setColumnControlVisible(true);
                                        jScrollPane2.setViewportView(detailTable);

                                        detailLayer.add(jScrollPane2, java.awt.BorderLayout.CENTER);

                                        coStatusBar.add(coStatusLabel);

                                        detailLayer.add(coStatusBar, java.awt.BorderLayout.PAGE_END);

                                        viewTabbedPane.addTab("View details", detailLayer);

                                        viewLayer.add(viewTabbedPane, java.awt.BorderLayout.CENTER);
                                        enableOnlyComponents(viewingComponents,true,sectionPane,usnPane,datePane,coPane,modulePane,plotButton);

                                        viewTabbedPane.addChangeListener(new ChangeListener() {
                                            public void stateChanged(ChangeEvent e) {

                                                if(viewTabbedPane.getSelectedIndex()==0)
                                                enableOnlyComponents(viewingComponents,true,sectionPane,usnPane,datePane,coPane,modulePane,plotButton);

                                                if(viewTabbedPane.getSelectedIndex()==1){
                                                    enableOnlyComponents(viewingComponents,false,plotButton,schedulePane);
                                                    if(scheduleToggle.isSelected())
                                                    scheduleToggle.doClick();
                                                }
                                            }
                                        });

                                        unregisterTab.addTab("View topic info", viewLayer);

                                        loginPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Student Login", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Agency FB", 1, 18))); // NOI18N

                                        loginUsnLabel.setText("USN");

                                        loginUSNField.setToolTipText("1hkXXcsXXX");
                                        loginUSNField.addActionListener(new java.awt.event.ActionListener() {
                                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                                loginUSNFieldActionPerformed(evt);
                                            }
                                        });

                                        loginPasswordLabel.setText("Password");

                                        loginButton.setText("Login");
                                        loginButton.addActionListener(new java.awt.event.ActionListener() {
                                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                                loginButtonActionPerformed(evt);
                                            }
                                        });

                                        javax.swing.GroupLayout loginPanelLayout = new javax.swing.GroupLayout(loginPanel);
                                        loginPanel.setLayout(loginPanelLayout);
                                        loginPanelLayout.setHorizontalGroup(
                                            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(loginPanelLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(loginUsnLabel)
                                                    .addComponent(loginPasswordLabel))
                                                .addGap(18, 18, 18)
                                                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(loginButton)
                                                    .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(loginPasswordField)
                                                        .addComponent(loginUSNField, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addContainerGap())
                                        );
                                        loginPanelLayout.setVerticalGroup(
                                            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(loginPanelLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(loginUsnLabel)
                                                    .addComponent(loginUSNField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(loginPasswordLabel)
                                                    .addComponent(loginPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addComponent(loginButton)
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        );

                                        enterLayer.setLayer(loginPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

                                        javax.swing.GroupLayout enterLayerLayout = new javax.swing.GroupLayout(enterLayer);
                                        enterLayer.setLayout(enterLayerLayout);
                                        enterLayerLayout.setHorizontalGroup(
                                            enterLayerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, enterLayerLayout.createSequentialGroup()
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(loginPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        );
                                        enterLayerLayout.setVerticalGroup(
                                            enterLayerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, enterLayerLayout.createSequentialGroup()
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(loginPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        );

                                        unregisterTab.addTab("Enter topic info", enterLayer);

                                        registerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Student Register", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Agency FB", 1, 18))); // NOI18N

                                        registerUSNLabel.setText("USN");

                                        registerUSNField.setToolTipText("1hkXXcsXXX");
                                        registerUSNField.addActionListener(new java.awt.event.ActionListener() {
                                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                                registerUSNFieldActionPerformed(evt);
                                            }
                                        });

                                        registerPasswordLabel.setText("Password");

                                        registerButton.setText("Register");
                                        registerButton.addActionListener(new java.awt.event.ActionListener() {
                                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                                registerButtonActionPerformed(evt);
                                            }
                                        });

                                        registerSectionLabel.setText("Section");

                                        registerSectionCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "unselected", "A", "B", "C" }));

                                        javax.swing.GroupLayout registerPanelLayout = new javax.swing.GroupLayout(registerPanel);
                                        registerPanel.setLayout(registerPanelLayout);
                                        registerPanelLayout.setHorizontalGroup(
                                            registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(registerPanelLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(registerPanelLayout.createSequentialGroup()
                                                        .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(registerUSNLabel)
                                                            .addComponent(registerSectionLabel))
                                                        .addGap(21, 21, 21)
                                                        .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(registerUSNField, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(registerSectionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGap(0, 0, Short.MAX_VALUE))
                                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, registerPanelLayout.createSequentialGroup()
                                                        .addComponent(registerPasswordLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(registerButton)
                                                            .addComponent(registerPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        );

                                        registerPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {registerPasswordField, registerUSNField});

                                        registerPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {registerPasswordLabel, registerSectionLabel, registerUSNLabel});

                                        registerPanelLayout.setVerticalGroup(
                                            registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(registerPanelLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(registerUSNLabel)
                                                    .addComponent(registerUSNField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(registerSectionLabel)
                                                    .addComponent(registerSectionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(21, 21, 21)
                                                .addGroup(registerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(registerPasswordLabel)
                                                    .addComponent(registerPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(registerButton)
                                                .addContainerGap(18, Short.MAX_VALUE))
                                        );

                                        registerLayer.setLayer(registerPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

                                        javax.swing.GroupLayout registerLayerLayout = new javax.swing.GroupLayout(registerLayer);
                                        registerLayer.setLayout(registerLayerLayout);
                                        registerLayerLayout.setHorizontalGroup(
                                            registerLayerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(registerLayerLayout.createSequentialGroup()
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(registerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        );
                                        registerLayerLayout.setVerticalGroup(
                                            registerLayerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, registerLayerLayout.createSequentialGroup()
                                                .addContainerGap(205, Short.MAX_VALUE)
                                                .addComponent(registerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(202, Short.MAX_VALUE))
                                        );

                                        unregisterTab.addTab("Register", registerLayer);

                                        unregisterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Student Unregister", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Agency FB", 1, 18))); // NOI18N

                                        unregisterUsnLabel.setText("USN");

                                        unregisterUSNField.addActionListener(new java.awt.event.ActionListener() {
                                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                                unregisterUSNFieldActionPerformed(evt);
                                            }
                                        });

                                        unregisterPasswordLabel.setText("Password");

                                        unregisterButton.setText("Unregister");
                                        unregisterButton.addActionListener(new java.awt.event.ActionListener() {
                                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                                unregisterButtonActionPerformed(evt);
                                            }
                                        });

                                        javax.swing.GroupLayout unregisterPanelLayout = new javax.swing.GroupLayout(unregisterPanel);
                                        unregisterPanel.setLayout(unregisterPanelLayout);
                                        unregisterPanelLayout.setHorizontalGroup(
                                            unregisterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(unregisterPanelLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(unregisterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(unregisterUsnLabel)
                                                    .addComponent(unregisterPasswordLabel))
                                                .addGap(18, 18, 18)
                                                .addGroup(unregisterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(unregisterButton)
                                                    .addGroup(unregisterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(unregisterPasswordField)
                                                        .addComponent(unregisterUSNField, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addContainerGap())
                                        );
                                        unregisterPanelLayout.setVerticalGroup(
                                            unregisterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(unregisterPanelLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(unregisterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(unregisterUsnLabel)
                                                    .addComponent(unregisterUSNField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(unregisterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(unregisterPasswordLabel)
                                                    .addComponent(unregisterPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addComponent(unregisterButton)
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        );

                                        unregisterLayer.setLayer(unregisterPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

                                        javax.swing.GroupLayout unregisterLayerLayout = new javax.swing.GroupLayout(unregisterLayer);
                                        unregisterLayer.setLayout(unregisterLayerLayout);
                                        unregisterLayerLayout.setHorizontalGroup(
                                            unregisterLayerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, unregisterLayerLayout.createSequentialGroup()
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(unregisterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        );
                                        unregisterLayerLayout.setVerticalGroup(
                                            unregisterLayerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, unregisterLayerLayout.createSequentialGroup()
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(unregisterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        );

                                        unregisterTab.addTab("Unregister", unregisterLayer);

                                        getContentPane().add(unregisterTab, java.awt.BorderLayout.CENTER);

                                        pack();
                                        setLocationRelativeTo(null);
                                    }// </editor-fold>//GEN-END:initComponents
//copy
    private void unregisterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unregisterButtonActionPerformed
        // TODO add your handling code here:
        if(gateway.isExistingUser(unregisterUSNField.getText(),new String (unregisterPasswordField.getPassword()))){
            gateway.deleteFromStudent(unregisterUSNField.getText());
            validUnregistrationOption.createDialog("valid unregistration").setVisible(true);
        }
        else
            invalidUnregistrationOption.createDialog("invalid unregistration").setVisible(true);
    }//GEN-LAST:event_unregisterButtonActionPerformed

    private void unregisterUSNFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unregisterUSNFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_unregisterUSNFieldActionPerformed

    private void registerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registerButtonActionPerformed
     //copy
        // TODO add your handling code here:
        if (!CheckHelper.checkEmpty(registerUSNField.getText(),new String(registerPasswordField.getPassword()),(String)registerSectionCombo.getSelectedItem())
                && CheckHelper.checkUsn(registerUSNField.getText())
                && !gateway.isExistingUser(registerUSNField.getText())){
            gateway.insertIntoStudent(registerUSNField.getText().toLowerCase(),new String(registerPasswordField.getPassword()),(String)registerSectionCombo.getSelectedItem());
            validRegistrationOption.createDialog("valid registration").setVisible(true);
        }
        else
            invalidRegistrationOption.createDialog("invalid registration").setVisible(true);
    }//GEN-LAST:event_registerButtonActionPerformed

    private void registerUSNFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registerUSNFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_registerUSNFieldActionPerformed

    private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
        // TODO add your handling code here:
        if(gateway.isExistingUser(loginUSNField.getText(),new String(loginPasswordField.getPassword()))){
            USN =loginUSNField.getText();
            if(!USN.equals("1hk16cs102")){

                adminStatusEntryRadio.setVisible(false);
                questionEntryRadio.setVisible(false);
                syllabusEntryRadio.setVisible(false);
                        
                iaEntryRadio.setVisible(true);
                statusEntryRadio.setVisible(true);                
                
            }
            else{

                adminStatusEntryRadio.setVisible(true);
                questionEntryRadio.setVisible(true);
                syllabusEntryRadio.setVisible(true);

                iaEntryRadio.setVisible(true);
                statusEntryRadio.setVisible(true);                

            }
            statusEntryRadio.setSelected(true);
            statusEntryRadio.setRequestFocusEnabled(true);
            entryFrame.setVisible(true);
            entryFrame.setSize(975, 504);
            entryFrame.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        }
        else
            invalidLoginOption.createDialog("Login Error").setVisible(true);
    }//GEN-LAST:event_loginButtonActionPerformed

    private void loginUSNFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginUSNFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_loginUSNFieldActionPerformed

    private void usnFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usnFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_usnFieldActionPerformed

    private void usnFieldMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_usnFieldMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_usnFieldMouseEntered

    private void usnFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_usnFieldMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_usnFieldMouseClicked
//copy
    private void viewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewButtonActionPerformed
        new SwingWorker<DefaultTableModel, Void>(){
            @Override
            protected DefaultTableModel doInBackground() throws Exception {
                setCoStatusLabel();
                return gateway.getSubjectDetails(viewer.asCompositeViewingQuery());
            }

            @Override
            protected void done() {
                try {
                    detailTableModel= get();
                    detailTable.setModel(detailTableModel);
                    //hide col 5
                    detailTable.getColumnExt(5).setVisible(false);
                    detailTable.setAutoCreateRowSorter(false);
                    BasicTableRowSorter sorter = new BasicTableRowSorter(detailTableModel);
                    detailTableModel.fireTableDataChanged();
                    sorter.setSortable(0, false);
                    sorter.setSortable(3, false);

                    sorter.setComparator(4,new EmptyComparator() , true);
                    sorter.setComparator(5,new EmptyComparator() , true);

                    detailTable.setRowSorter(sorter);                }       
                catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }//GEN-LAST:event_viewButtonActionPerformed



    private void entryPageFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entryPageFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_entryPageFieldActionPerformed

    private void entryEnterButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_entryEnterButtonMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_entryEnterButtonMouseClicked

    private void entryEnterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entryEnterButtonActionPerformed
        // TODO add your handling code here:
        if(enterer.send())
            validEntryOption.createDialog("valid entry").setVisible(true);
        else
            invalidEntryOption.createDialog("Invalid entry").setVisible(true);          
    }//GEN-LAST:event_entryEnterButtonActionPerformed

    private void entryTopicComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entryTopicComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_entryTopicComboActionPerformed

    private void entryModuleComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entryModuleComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_entryModuleComboActionPerformed

    private void adminStatusEntryRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminStatusEntryRadioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_adminStatusEntryRadioActionPerformed

    private void subjectComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subjectComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_subjectComboActionPerformed

    private void entryCoveredTopicComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entryCoveredTopicComboActionPerformed
                    
                    
    }//GEN-LAST:event_entryCoveredTopicComboActionPerformed

    private void entryCoveredTopicComboFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_entryCoveredTopicComboFocusGained

        
    }//GEN-LAST:event_entryCoveredTopicComboFocusGained

    private void entryCoveredTopicComboPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_entryCoveredTopicComboPopupMenuWillBecomeVisible
        setCombo(entryCoveredTopicCombo, entryViewer.withBase(new TopicsCoveredBase()));
    }//GEN-LAST:event_entryCoveredTopicComboPopupMenuWillBecomeVisible

    private void entryQuestionComboPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_entryQuestionComboPopupMenuWillBecomeVisible
        // TODO add your handling code here:
        setCombo(entryQuestionCombo,entryViewer.withBase(new QuestionsCoveredBase()));
    }//GEN-LAST:event_entryQuestionComboPopupMenuWillBecomeVisible

    private void entryTextbookComboPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_entryTextbookComboPopupMenuWillBecomeVisible
        setCombo(entryTextbookCombo, entryViewer.withBase(new TextbookBase()));
    }//GEN-LAST:event_entryTextbookComboPopupMenuWillBecomeVisible

    private void entryTopicComboPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_entryTopicComboPopupMenuWillBecomeVisible
        // TODO add your handling code here:
        setCombo(entryTopicCombo, entryViewer.withBase(new TopicBase()));        
    }//GEN-LAST:event_entryTopicComboPopupMenuWillBecomeVisible

    private void dateRangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateRangeButtonActionPerformed
        // TODO add your handling code here:
        distributeTable();
    }//GEN-LAST:event_dateRangeButtonActionPerformed

    private void scheduleToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scheduleToggleActionPerformed
        // TODO add your handling code here:
if(scheduleToggle.isSelected())
    schedulePane.setVisible(true);
else
    schedulePane.setVisible(false);

    }//GEN-LAST:event_scheduleToggleActionPerformed

    private void reportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reportButtonActionPerformed
        try {
            // TODO add your handling code here:
            //JasperReport ssmsReport=JasperCompileManager.compileReport();
            JRDataSource dataSource=new JRTableModelDataSource(new TableModelDecorator(detailTable.getModel(),detailTable));
            JasperPrint ssmsReportPrinter=JasperFillManager.fillReport(getClass().getResourceAsStream("/resources/ssmsReport.jasper"),null,dataSource);
            JasperViewer ssmsReportViewer=new JasperViewer(ssmsReportPrinter,false);
            ssmsReportViewer.setVisible(true);
            ssmsReportViewer.setAlwaysOnTop(true);
        } catch (JRException ex) {
            Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_reportButtonActionPerformed

    private void entrySubjectComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entrySubjectComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_entrySubjectComboActionPerformed

    private void entrySubjectComboPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_entrySubjectComboPopupMenuWillBecomeVisible
        // TODO add your handling code here:
        setCombo(entrySubjectCombo,entryViewer.withBase(new SubjectBase()));
    }//GEN-LAST:event_entrySubjectComboPopupMenuWillBecomeVisible

    private void subjectComboPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_subjectComboPopupMenuWillBecomeVisible
        subjectCombo.removeItemListener(subjectComboListener);
        viewer=viewer.withInputFilters(new SubjectFilter("unselected"));                              
        setCombo(subjectCombo,viewer.withBase(new SubjectsCoveredBase()));
        subjectCombo.addItemListener(subjectComboListener);
    }//GEN-LAST:event_subjectComboPopupMenuWillBecomeVisible

    private void entrySemesterComboPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_entrySemesterComboPopupMenuWillBecomeVisible
        // TODO add your handling code here:
    }//GEN-LAST:event_entrySemesterComboPopupMenuWillBecomeVisible

    private void entrySemesterComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entrySemesterComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_entrySemesterComboActionPerformed

    private void subjectComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_subjectComboItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_subjectComboItemStateChanged

    private void summaryCanvasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_summaryCanvasKeyPressed
        // TODO add your handling code here:    
    }//GEN-LAST:event_summaryCanvasKeyPressed

    private void viewTabbedPaneComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_viewTabbedPaneComponentShown
        // TODO add your handling code here:
    }//GEN-LAST:event_viewTabbedPaneComponentShown

    private void plotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plotButtonActionPerformed
        // TODO add your handling code here:
      render.updateDataPoints(viewer);
    }//GEN-LAST:event_plotButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(UserInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UserInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UserInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UserInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                    new UserInterface().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox abstractionLevelCombo;
    private javax.swing.JLayeredPane abstractionPane;
    private javax.swing.JRadioButton adminStatusEntryRadio;
    private javax.swing.JComboBox coCombo;
    private javax.swing.JLabel coLabel;
    private javax.swing.JLabel coLabel1;
    private javax.swing.JLayeredPane coPane;
    private org.jdesktop.swingx.JXStatusBar coStatusBar;
    private javax.swing.JLabel coStatusLabel;
    private javax.swing.JLayeredPane datePane;
    private javax.swing.JButton dateRangeButton;
    private javax.swing.JLayeredPane detailLayer;
    private org.jdesktop.swingx.JXTable detailTable;
    private javax.swing.JComboBox<String> difficultyCombo;
    private javax.swing.JLabel difficultyLabel;
    private javax.swing.JLayeredPane difficultyPane;
    private javax.swing.JLayeredPane enterLayer;
    private javax.swing.JLabel entryAbstractionLabel;
    public javax.swing.JComboBox<String> entryAbstractionLevelCombo;
    private org.jdesktop.swingx.JXPanel entryAbstractionPanel;
    private javax.swing.JTextField entryCoField;
    private javax.swing.JLabel entryCoLabel;
    private org.jdesktop.swingx.JXPanel entryCoPanel;
    public javax.swing.JComboBox<String> entryCoveredTopicCombo;
    private org.jdesktop.swingx.JXPanel entryCoveredTopicPanel;
    private datechooser.beans.DateChooserCombo entryDateField;
    private javax.swing.JLabel entryDateLabel;
    private org.jdesktop.swingx.JXPanel entryDatePanel;
    public javax.swing.JComboBox<String> entryDifficultyCombo;
    private javax.swing.JLabel entryDifficultyLabel;
    private org.jdesktop.swingx.JXPanel entryDifficultyPanel;
    private javax.swing.JButton entryEnterButton;
    private javax.swing.JFrame entryFrame;
    private javax.swing.JTextField entryIaMarksField;
    private javax.swing.JLabel entryIaMarksLabel;
    private org.jdesktop.swingx.JXPanel entryIaMarksPanel;
    private javax.swing.JComboBox<String> entryModuleCombo;
    private javax.swing.JLabel entryModuleLabel;
    private org.jdesktop.swingx.JXPanel entryModulePanel;
    private org.jdesktop.swingx.JXPanel entryNewQuestionPanel;
    private org.jdesktop.swingx.JXPanel entryNewTopicPanel;
    private javax.swing.JTextField entryPageField;
    private javax.swing.JLabel entryPageLabel;
    private org.jdesktop.swingx.JXPanel entryPagePanel;
    public javax.swing.JTextArea entryQuestionArea;
    private javax.swing.JComboBox<String> entryQuestionCombo;
    private javax.swing.JLabel entryQuestionLabel;
    private org.jdesktop.swingx.JXPanel entryQuestionPanel;
    private javax.swing.JComboBox<String> entrySemesterCombo;
    private javax.swing.JLabel entrySemesterLabel;
    private org.jdesktop.swingx.JXPanel entrySemesterPanel;
    private javax.swing.JComboBox<String> entrySubjectCombo;
    private javax.swing.JLabel entrySubjectLabel;
    private org.jdesktop.swingx.JXPanel entrySubjectPanel;
    private javax.swing.JComboBox<String> entryTextbookCombo;
    private javax.swing.JLabel entryTextbookLabel;
    private org.jdesktop.swingx.JXPanel entryTextbookPanel;
    public javax.swing.JComboBox<String> entryTopicCombo;
    private javax.swing.JLabel entryTopicComboLabel;
    private javax.swing.JTextField entryTopicField;
    private javax.swing.JLabel entryTopicFieldLabel;
    private org.jdesktop.swingx.JXPanel entryTopicPanel;
    public javax.swing.JTextField entryTotalMarksField;
    private javax.swing.JLabel entryTotalMarksLabel;
    private org.jdesktop.swingx.JXPanel entryTotalMarksPanel;
    private javax.swing.ButtonGroup entryTypeGroup;
    private org.jdesktop.swingx.JXPanel entryTypePanel;
    private datechooser.beans.DateChooserCombo finalDateField;
    private datechooser.beans.DateChooserCombo finalDateField1;
    private javax.swing.JLabel finalDateLabel;
    private javax.swing.JLabel finalDateLabel1;
    private javax.swing.JRadioButton iaEntryRadio;
    private datechooser.beans.DateChooserCombo initialDateField;
    private datechooser.beans.DateChooserCombo initialDateField1;
    private javax.swing.JLabel initialDateLabel;
    private javax.swing.JLabel initialDateLabel1;
    public javax.swing.JOptionPane invalidEntryOption;
    private javax.swing.JOptionPane invalidLoginOption;
    private javax.swing.JOptionPane invalidRegistrationOption;
    private javax.swing.JOptionPane invalidUnregistrationOption;
    private javax.swing.JLabel invalidUsnLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel legendLabel;
    private javax.swing.JButton loginButton;
    private javax.swing.JPanel loginPanel;
    private javax.swing.JPasswordField loginPasswordField;
    private javax.swing.JLabel loginPasswordLabel;
    private javax.swing.JTextField loginUSNField;
    private javax.swing.JLabel loginUsnLabel;
    private javax.swing.JComboBox<String> moduleCombo;
    private javax.swing.JLabel moduleLabel;
    private javax.swing.JLayeredPane modulePane;
    private javax.swing.JButton plotButton;
    private org.jdesktop.swingx.JXStatusBar plotStatusBar;
    private javax.swing.JRadioButton questionEntryRadio;
    private javax.swing.JButton registerButton;
    private javax.swing.JLayeredPane registerLayer;
    private javax.swing.JPanel registerPanel;
    private javax.swing.JPasswordField registerPasswordField;
    private javax.swing.JLabel registerPasswordLabel;
    private javax.swing.JComboBox<String> registerSectionCombo;
    private javax.swing.JLabel registerSectionLabel;
    private javax.swing.JTextField registerUSNField;
    private javax.swing.JLabel registerUSNLabel;
    private javax.swing.JButton reportButton;
    private javax.swing.JLayeredPane schedulePane;
    private javax.swing.JToggleButton scheduleToggle;
    private javax.swing.JComboBox<String> sectionCombo;
    private javax.swing.JLabel sectionLabel;
    private javax.swing.JLayeredPane sectionPane;
    private javax.swing.JComboBox<String> semesterCombo;
    private javax.swing.JLabel semesterLabel;
    private javax.swing.JLayeredPane semesterPane;
    private javax.swing.JRadioButton statusEntryRadio;
    private javax.swing.JComboBox<String> subjectCombo;
    private javax.swing.JLabel subjectLabel;
    private javax.swing.JLayeredPane subjectPane;
    /*
    private org.netbeans.modules.form.InvalidComponent summaryCanvas;
    */
    private GLCanvas summaryCanvas=new GLCanvas();
    private Render render=new Render(summaryCanvas,this::onBubbleClick);
    private javax.swing.JLayeredPane summaryLayer;
    private javax.swing.JRadioButton syllabusEntryRadio;
    private javax.swing.JButton unregisterButton;
    private javax.swing.JLayeredPane unregisterLayer;
    private javax.swing.JPanel unregisterPanel;
    private javax.swing.JPasswordField unregisterPasswordField;
    private javax.swing.JLabel unregisterPasswordLabel;
    private javax.swing.JTabbedPane unregisterTab;
    private javax.swing.JTextField unregisterUSNField;
    private javax.swing.JLabel unregisterUsnLabel;
    private javax.swing.JTextField usnField;
    private javax.swing.JLabel usnLabel;
    private javax.swing.JLayeredPane usnPane;
    public javax.swing.JOptionPane validEntryOption;
    private javax.swing.JOptionPane validRegistrationOption;
    private javax.swing.JOptionPane validUnregistrationOption;
    private javax.swing.JButton viewButton;
    private javax.swing.JLayeredPane viewLayer;
    private javax.swing.JTabbedPane viewTabbedPane;
    // End of variables declaration//GEN-END:variables
}
