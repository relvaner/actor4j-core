/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.analyze;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Map;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import actor4j.core.ActorCell;
import actor4j.core.ActorSystem;

public class VisualActorFrame extends JFrame {
	protected static final long serialVersionUID = 6808210435112913511L;
	
	protected ActorSystem system;

	protected JPanel contentPane;
	
	protected VisualActorViewPanel leftViewPanel;
	protected VisualActorViewPanel rightViewPanel;
	
	public VisualActorFrame(ActorSystem system) {
		super();
		
		this.system = system;
		
		initialize();
	}
	
	public void initialize() {
		setBounds(0, 0, 1024, 768);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel paContent = new JPanel();
		contentPane.add(paContent, BorderLayout.CENTER);
		paContent.setLayout(new GridLayout(1, 2, 0, 0));
		
		leftViewPanel  = new VisualActorStructureViewPanel(system);
		rightViewPanel = new VisualActorBehaviourViewPanel(system);
		
		paContent.add(leftViewPanel);
		paContent.add(rightViewPanel);
	}
	
	public void analyzeStructure(Map<UUID, ActorCell> actorCells, boolean showDefaultParent) {
		((VisualActorStructureViewPanel)leftViewPanel).analyzeStructure(actorCells, showDefaultParent);
		((VisualActorStructureViewPanel)leftViewPanel).updateStructure();
	}
	
	public void analyzeBehaviour(Map<UUID, ActorCell> actorCells, Map<UUID, Map<UUID, Long>> deliveryRoutes) {
		((VisualActorBehaviourViewPanel)rightViewPanel).analyzeBehaviour(actorCells, deliveryRoutes);
		((VisualActorBehaviourViewPanel)rightViewPanel).updateStructure();
	}
}
