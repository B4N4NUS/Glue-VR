package dev.slimevr.gui;

import java.awt.*;
import java.util.List;

import javax.swing.*;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import dev.slimevr.VRServer;
import dev.slimevr.gui.swing.EJBagNoStretch;
import dev.slimevr.gui.swing.EJPanel;
import dev.slimevr.util.ann.VRServerThread;
import dev.slimevr.vr.processor.TransformNode;
import dev.slimevr.vr.processor.skeleton.HumanSkeleton;
import io.eiren.util.StringUtils;
import io.eiren.util.ann.ThreadSafe;
import io.eiren.util.collections.FastList;

public class SkeletonList extends JPanel {
	
	private static final long UPDATE_DELAY = 50;

	Quaternion q = new Quaternion();
	Vector3f v = new Vector3f();
	float[] angles = new float[3];
	
	private final VRServerGUI gui;
	public final List<NodeStatus> nodes = new FastList<>();
	private long lastUpdate = 0;
	
	public SkeletonList(VRServer server, VRServerGUI gui) {
		//super(false, true);
		this.gui = gui;

		setLayout(new GridBagLayout());
		setAlignmentY(TOP_ALIGNMENT);
		server.addSkeletonUpdatedCallback(this::skeletonUpdated);
	}
	
	@ThreadSafe
	public void skeletonUpdated(HumanSkeleton newSkeleton) {
		java.awt.EventQueue.invokeLater(() -> {
			removeAll();
			nodes.clear();
			
			add(new JLabel("Joint"), EJPanel.c(0, 0, 2));
			add(new JLabel("X"), EJPanel.c(1, 0, 2));
			add(new JLabel("Y"), EJPanel.c(2, 0, 2));
			add(new JLabel("Z"), EJPanel.c(3, 0, 2));
			add(new JLabel("Pitch"), EJPanel.c(4, 0, 2));
			add(new JLabel("Yaw"), EJPanel.c(5, 0, 2));
			add(new JLabel("Roll"), EJPanel.c(6, 0, 2));
			
			newSkeleton.getRootNode().depthFirstTraversal((node) -> {
				int n = nodes.size();
				nodes.add(new NodeStatus(node, n + 1));
			});
			
			
			gui.refresh();
		});
	}

	@VRServerThread
	public void updateBones() {
		if(lastUpdate + UPDATE_DELAY > System.currentTimeMillis())
			return;
		lastUpdate = System.currentTimeMillis();
		java.awt.EventQueue.invokeLater(() -> {
			for(int i = 0; i < nodes.size(); ++i)
				nodes.get(i).update();
		});
	}
	
	public class NodeStatus {

		TransformNode n;
		public JLabel x;
		public JLabel y;
		public JLabel z;
		public JLabel a1;
		public JLabel a2;
		public JLabel a3;
		
		public NodeStatus(TransformNode node, int n) {
			this.n = node;
			add(new JLabel(node.getName()), EJPanel.c(0, n, 2, GridBagConstraints.FIRST_LINE_START));
			add(x = new JLabel("0"), EJPanel.c(1, n, 2, GridBagConstraints.FIRST_LINE_START));
			add(y = new JLabel("0"), EJPanel.c(2, n, 2, GridBagConstraints.FIRST_LINE_START));
			add(z = new JLabel("0"), EJPanel.c(3, n, 2, GridBagConstraints.FIRST_LINE_START));
			add(a1 = new JLabel("0"), EJPanel.c(4, n, 2, GridBagConstraints.FIRST_LINE_START));
			add(a2 = new JLabel("0"), EJPanel.c(5, n, 2, GridBagConstraints.FIRST_LINE_START));
			add(a3 = new JLabel("0"), EJPanel.c(6, n, 2, GridBagConstraints.FIRST_LINE_START));
		}
		
		public void update() {
			n.worldTransform.getTranslation(v);
			n.worldTransform.getRotation(q);
			q.toAngles(angles);
			
			x.setText(StringUtils.prettyNumber(v.x, 2));
			y.setText(StringUtils.prettyNumber(v.y, 2));
			z.setText(StringUtils.prettyNumber(v.z, 2));
			a1.setText(StringUtils.prettyNumber(angles[0] * FastMath.RAD_TO_DEG, 0));
			a2.setText(StringUtils.prettyNumber(angles[1] * FastMath.RAD_TO_DEG, 0));
			a3.setText(StringUtils.prettyNumber(angles[2] * FastMath.RAD_TO_DEG, 0));
		}
		@Override
		public String toString() {
			return n.getName();
		}
	}
}
