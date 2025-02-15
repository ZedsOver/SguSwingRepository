package main_app.swing;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

/**
 * This class makes it easy to drag and drop files from the operating system to
 * a Java program. Any <tt>java.awt.Component</tt> can be dropped onto, but only
 * <tt>javax.swing.JComponent</tt>s will indicate the drop event with a changed
 * border.
 * <p/>
 * To use this class, construct a new <tt>FileDrop</tt> by passing it the target
 * component and a <tt>Listener</tt> to receive notification when file(s) have
 * been dropped. Here is an example:
 * <p/>
 * <code><pre>
 *      JPanel myPanel = new JPanel();
 *      new FileDrop( myPanel, new FileDrop.Listener()
 *      {   public void filesDropped( java.io.File[] files )
 *          {
 *              // handle file drop
 *              ...
 *          }   // end filesDropped
 *      }); // end FileDrop.Listener
 * </pre></code>
 * <p/>
 * You can specify the border that will appear when files are being dragged by
 * calling the constructor with a <tt>javax.swing.border.Border</tt>. Only
 * <tt>JComponent</tt>s will show any indication with a border.
 * <p/>
 * You can turn on some debugging features by passing a <tt>PrintStream</tt>
 * object (such as <tt>System.out</tt>) into the full constructor. A
 * <tt>null</tt>
 * value will result in no extra debugging information being output.
 * <p/>
 *
 * <p>
 * I'm releasing this code into the Public Domain. Enjoy.
 * </p>
 * <p>
 * Original author: Robert Harder, rob@iharder.net</p>
 * <p>
 * Additional support: </p>
 * <ul>
 * <li>September 2007, Nathan Blomquist -- Linux (KDE/Gnome) support added.</li>
 * <li>December 2010, Joshua Gerth</li>
 * </ul>
 *
 * @author Robert Harder
 * @author rharder@users.sf.net
 * @version 1.1.1
 * @hidden
 * @exclude
 */
public abstract class FileDrop {

    private transient java.awt.dnd.DropTargetListener dropListener;

    /**
     * Discover if the running JVM is modern enough to have drag and drop.
     */
    private static Boolean supportsDnD;

    /**
     * Constructs a {@link FileDrop} with a default light-blue border and, if
     * <var>c</var> is a {@link java.awt.Container}, recursively sets all
     * elements contained within as drop targets, though only the top level
     * container will change borders.
     *
     * @param c Component on which files will be dropped.
     * @since 1.0
     */
    public FileDrop(
            final java.awt.Component c)
    {
        this(null, // Logging stream
                c, // Drop target
                true // Recursive
        );
    }   // end constructor

    /**
     * Constructor with a default border and the option to recursively set drop
     * targets. If your component is a <tt>java.awt.Container</tt>, then each of
     * its children components will also listen for drops, though only the
     * parent will change borders.
     *
     * @param c Component on which files will be dropped.
     * @param recursive Recursively set children as drop targets.
     * @param listener Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    public FileDrop(
            final java.awt.Component c,
            final boolean recursive)
    {
        this(null, // Logging stream
                c, // Drop target
                recursive // Recursive
        );
    }   // end constructor

    /**
     * Constructor with a default border and debugging optionally turned on.
     * With Debugging turned on, more status messages will be displayed to
     * <tt>out</tt>. A common way to use this constructor is with
     * <tt>System.out</tt> or <tt>System.err</tt>. A <tt>null</tt> value for the
     * parameter <tt>out</tt> will result in no debugging output.
     *
     * @param out PrintStream to record debugging info or null for no debugging.
     * @param c Component on which files will be dropped.
     * @param listener Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    public FileDrop(
            final java.io.PrintStream out,
            final java.awt.Component c
    )
    {
        this(out, // Logging stream
                c, // Drop target
                false // Recursive
        );
    }   // end constructor

    /**
     * Full constructor with a specified border and debugging optionally turned
     * on. With Debugging turned on, more status messages will be displayed to
     * <tt>out</tt>. A common way to use this constructor is with
     * <tt>System.out</tt> or <tt>System.err</tt>. A <tt>null</tt> value for the
     * parameter <tt>out</tt> will result in no debugging output.
     *
     * @param out PrintStream to record debugging info or null for no debugging.
     * @param c Component on which files will be dropped.
     * @param recursive Recursively set children as drop targets.
     * @param listener Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    public FileDrop(
            final java.io.PrintStream out,
            final java.awt.Component c,
            final boolean recursive)
    {
        if (supportsDnD()) {   // Make a drop listener
            dropListener = new java.awt.dnd.DropTargetListener() {
                Point pus;

                @Override
                public void dragEnter(java.awt.dnd.DropTargetDragEvent evt)
                {
                    log(out, "FileDrop: Enter event.");
                    pus = evt.getLocation();
                    onEnter(pus.x, pus.y);
                }

                @Override
                public void dragOver(java.awt.dnd.DropTargetDragEvent evt)
                {
                    log(out, "FileDrop: Over event.");
                    pus = evt.getLocation();
                    onMove(pus.x, pus.y);
                }

                @Override
                public void drop(java.awt.dnd.DropTargetDropEvent evt)
                {
                    log(out, "FileDrop: drop event.");
                    try {   // Get whatever was dropped
                        java.awt.datatransfer.Transferable tr = evt.getTransferable();

                        // Is it a file list?
                        if (tr.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.javaFileListFlavor)) {
                            // Say we'll take it.
                            //evt.acceptDrop ( java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE );
                            evt.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
                            log(out, "FileDrop: file list accepted.");

                            // Get a useful list
                            java.util.List fileList = (java.util.List) tr.getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
                            // Convert list to array
                            java.io.File[] filesTemp = new java.io.File[fileList.size()];
                            fileList.toArray(filesTemp);
                            final java.io.File[] files = filesTemp;

                            // Alert listener to drop.
                            pus = evt.getLocation();
                            filesDropped(pus.x, pus.y, files);

                            // Mark that drop is completed.
                            evt.getDropTargetContext().dropComplete(true);
                            log(out, "FileDrop: drop complete.");
                        } // end if: file list
                        else // this section will check for a reader flavor.
                        {
                            // Thanks, Nathan!
                            // BEGIN 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.
                            DataFlavor[] flavors = tr.getTransferDataFlavors();
                            boolean handled = false;
                            for (int zz = 0; zz < flavors.length; zz++) {
                                if (flavors[zz].isRepresentationClassReader()) {
                                    // Say we'll take it.
                                    //evt.acceptDrop ( java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE );
                                    evt.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
                                    log(out, "FileDrop: reader accepted.");

                                    Reader reader = flavors[zz].getReaderForText(tr);

                                    BufferedReader br = new BufferedReader(reader);

                                    pus = evt.getLocation();
                                    filesDropped(pus.x, pus.y, createFileArray(br, out));

                                    // Mark that drop is completed.
                                    evt.getDropTargetContext().dropComplete(true);
                                    log(out, "FileDrop: drop complete.");
                                    handled = true;
                                    break;
                                }
                            }
                            if (!handled) {
                                log(out, "FileDrop: not a file list or reader - abort.");
                                evt.rejectDrop();
                            }
                            // END 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.
                        }   // end else: not a file list
                    } // end try
                    catch (java.io.IOException io) {
                        log(out, "FileDrop: IOException - abort:");
                        io.printStackTrace(out == null ? System.err : out);
                        try {
                            evt.rejectDrop();
                        }
                        catch (Exception e) {
                        }
                    } // end catch IOException
                    catch (java.awt.datatransfer.UnsupportedFlavorException ufe) {
                        log(out, "FileDrop: UnsupportedFlavorException - abort:");
                        ufe.printStackTrace(out == null ? System.err : out);
                        try {
                            evt.rejectDrop();
                        }
                        catch (Exception e) {
                        }
                    } // end catch: UnsupportedFlavorException

                }   // end drop

                @Override
                public void dragExit(java.awt.dnd.DropTargetEvent evt)
                {
                    log(out, "FileDrop: Exit event.");
                    onExit(pus.x, pus.y);
                }   // end dragExit

                @Override
                public void dropActionChanged(java.awt.dnd.DropTargetDragEvent evt)
                {
                    log(out, "FileDrop: dropActionChanged event.");
                    // Is this an acceptable drag event?
                    if (isDragOk(out, evt)) {   //evt.acceptDrag( java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE );
                        evt.acceptDrag(java.awt.dnd.DnDConstants.ACTION_COPY);
                        log(out, "FileDrop: event accepted.");
                    } // end if: drag ok
                    else {
                        evt.rejectDrag();
                        log(out, "FileDrop: event rejected.");
                    }   // end else: drag not ok
                }   // end dropActionChanged
            }; // end DropTargetListener

            // Make the component (and possibly children) drop targets
            makeDropTarget(out, c, recursive);
        } // end if: supports dnd
        else {
            log(out, "FileDrop: Drag and drop is not supported with this JVM");
        }   // end else: does not support DnD
    }   // end constructor

    protected abstract void onEnter(int x, int y);

    protected abstract void onMove(int x, int y);

    protected abstract void filesDropped(int x, int y, File[] files);

    protected abstract void onExit(int x, int y);

    private static boolean supportsDnD()
    {   // Static Boolean
        if (supportsDnD == null) {
            boolean support = false;
            try {
                Class.forName("java.awt.dnd.DnDConstants");
                support = true;
            } // end try
            catch (Exception e) {
                support = false;
            }   // end catch
            supportsDnD = new Boolean(support);
        }   // end if: first time through
        return supportsDnD.booleanValue();
    }   // end supportsDnD

    // BEGIN 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.
    private static String ZERO_CHAR_STRING = "" + (char) 0;

    public static File[] createFileArray(BufferedReader bReader, PrintStream out)
    {
        try {
            java.util.List list = new java.util.ArrayList();
            java.lang.String line = null;
            while ((line = bReader.readLine()) != null) {
                try {
                    // kde seems to append a 0 char to the end of the reader
                    if (ZERO_CHAR_STRING.equals(line)) {
                        continue;
                    }

                    java.io.File file = new java.io.File(new java.net.URI(line));
                    list.add(file);
                }
                catch (Exception ex) {
                    log(out, "Error with " + line + ": " + ex.getMessage());
                }
            }

            return (java.io.File[]) list.toArray(new File[list.size()]);
        }
        catch (IOException ex) {
            log(out, "FileDrop: IOException");
        }
        return new File[0];
    }
    // END 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.

    private void makeDropTarget(final java.io.PrintStream out, final java.awt.Component c, boolean recursive)
    {
        // Make drop target
        final java.awt.dnd.DropTarget dt = new java.awt.dnd.DropTarget();
        try {
            dt.addDropTargetListener(dropListener);
        } // end try
        catch (java.util.TooManyListenersException e) {
            e.printStackTrace();
            log(out, "FileDrop: Drop will not work due to previous error. Do you have another listener attached?");
        }   // end catch

        // Listen for hierarchy changes and remove the drop target when the parent gets cleared out.
        c.addHierarchyListener(new java.awt.event.HierarchyListener() {
            @Override
            public void hierarchyChanged(java.awt.event.HierarchyEvent evt)
            {
                log(out, "FileDrop: Hierarchy changed.");
                java.awt.Component parent = c.getParent();
                if (parent == null) {
                    c.setDropTarget(null);
                    log(out, "FileDrop: Drop target cleared from component.");
                } // end if: null parent
                else {
                    new java.awt.dnd.DropTarget(c, dropListener);
                    log(out, "FileDrop: Drop target added to component.");
                }   // end else: parent not null
            }   // end hierarchyChanged
        }); // end hierarchy listener
        if (c.getParent() != null) {
            new java.awt.dnd.DropTarget(c, dropListener);
        }

        if (recursive && (c instanceof java.awt.Container)) {
            // Get the container
            java.awt.Container cont = (java.awt.Container) c;

            // Get it's components
            java.awt.Component[] comps = cont.getComponents();

            // Set it's components as listeners also
            for (int i = 0; i < comps.length; i++) {
                makeDropTarget(out, comps[i], recursive);
            }
        }   // end if: recursively set components as listener
    }   // end dropListener

    /**
     * Determine if the dragged data is a file list.
     */
    private boolean isDragOk(final java.io.PrintStream out, final java.awt.dnd.DropTargetDragEvent evt)
    {
        boolean ok = false;

        // Get data flavors being dragged
        java.awt.datatransfer.DataFlavor[] flavors = evt.getCurrentDataFlavors();

        // See if any of the flavors are a file list
        int i = 0;
        while (!ok && i < flavors.length) {
            // BEGIN 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.
            // Is the flavor a file list?
            final DataFlavor curFlavor = flavors[i];
            if (curFlavor.equals(java.awt.datatransfer.DataFlavor.javaFileListFlavor)
                    || curFlavor.isRepresentationClassReader()) {
                ok = true;
            }
            // END 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.
            i++;
        }   // end while: through flavors

        // If logging is enabled, show data flavors
        if (out != null) {
            if (flavors.length == 0) {
                log(out, "FileDrop: no data flavors.");
            }
            for (i = 0; i < flavors.length; i++) {
                log(out, flavors[i].toString());
            }
        }   // end if: logging enabled

        return ok;
    }   // end isDragOk

    /**
     * Outputs <tt>message</tt> to <tt>out</tt> if it's not null.
     */
    private static void log(java.io.PrintStream out, String message)
    {   // Log message if requested
        if (out != null) {
            out.println(message);
        }
    }   // end log

    /**
     * Removes the drag-and-drop hooks from the component and optionally from
     * the all children. You should call this if you add and remove components
     * after you've set up the drag-and-drop. This will recursively unregister
     * all components contained within
     * <var>c</var> if <var>c</var> is a {@link java.awt.Container}.
     *
     * @param c The component to unregister as a drop target
     * @since 1.0
     */
    public static boolean remove(java.awt.Component c)
    {
        return remove(null, c, true);
    }   // end remove

    /**
     * Removes the drag-and-drop hooks from the component and optionally from
     * the all children. You should call this if you add and remove components
     * after you've set up the drag-and-drop.
     *
     * @param out Optional {@link java.io.PrintStream} for logging drag and drop
     * messages
     * @param c The component to unregister
     * @param recursive Recursively unregister components within a container
     * @since 1.0
     */
    public static boolean remove(java.io.PrintStream out, java.awt.Component c, boolean recursive)
    {   // Make sure we support dnd.
        if (supportsDnD()) {
            log(out, "FileDrop: Removing drag-and-drop hooks.");
            c.setDropTarget(null);
            if (recursive && (c instanceof java.awt.Container)) {
                java.awt.Component[] comps = ((java.awt.Container) c).getComponents();
                for (int i = 0; i < comps.length; i++) {
                    remove(out, comps[i], recursive);
                }
                return true;
            } // end if: recursive
            else {
                return false;
            }
        } // end if: supports DnD
        else {
            return false;
        }
    }   // end remove

}   // end class FileDrop
