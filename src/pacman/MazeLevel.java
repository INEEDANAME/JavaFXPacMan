package pacman;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Created by dylan on 2016-04-03.
 *
 * MazeLevel.java
 *
 * This is responsible for creating all level and game technical and interactive components:
 * Buttons, PacDots, Text, Collision Boundaries, Entities & Related Image / ImageView
 *
 * Also the SVG Path data movement and animation + timeline is handled with this file
 */
public class MazeLevel {

    //MazeScene
    private Scene maze;
    private StackPane paneMaze;

    private Button bttnStart;
    private Text ScoreBoard;

    //MazeData
    private Rectangle[] mazeData;
    private Circle[] pacdots;
    private Bounds[] pacDotBounds;
    private Bounds pacManBounds;

    //TODO Use an Array for the Ghosts - See about Array for ImageViews
    //Maze Entities
    private PacMan pacMan;
    private ImageView pacManImgView;
    private Ghost blinky;
    private ImageView blinkyImgView;
    private Ghost pinky;
    private ImageView pinkyImgView;

    //ANIMATION

    private Timeline tl;
    private KeyFrame kf;
    private PathTransition pth;
   private PathTransition pinkyPath;

    //MAIN SPRITE IMAGE
    private Image spriteImage;

    //Level PacDot Number Count
    protected int pacdotCount;

    //TODO Review and optimize this constructor and separate out old / unnecessary code
    public MazeLevel() {

        this.mazeData = getMaze();
        this.paneMaze = generateMazeLevel();

        this.paneMaze.setStyle("-fx-background-color: black");

        this.spriteImage = getSpriteImagePath("assets/img/PacManSprite.png");

        this.blinky = new Ghost(GhostNames.BLINKY);
        this.blinkyImgView = new ImageView(this.spriteImage);
        this.blinkyImgView.setViewport(this.blinky.getSprite());

        this.pinky = new Ghost(GhostNames.PINKY);
        this.pinkyImgView = new ImageView(this.spriteImage);
        this.pinkyImgView.setViewport(this.pinky.getSprite());

        this.pacManImgView = new ImageView(this.spriteImage);
        this.pacMan = new PacMan(this.mazeData, this.blinkyImgView, this.pinkyImgView);
        this.pacMan.setup(this.pacManImgView);
        this.pacManImgView.setViewport(this.pacMan.getSprite());


        this.pacDotBounds = new Bounds[this.pacdots.length];

        for (int i = 0; i < this.pacdots.length; i++) {
            this.pacdots[i].setFill(Color.YELLOW);
            this.pacDotBounds[i] = this.pacdots[i].getBoundsInParent();
        }
        this.pacdotCount = this.pacdots.length;

        this.bttnStart = new Button("Start Game!");
        this.bttnStart.setLayoutX(this.paneMaze.getWidth() / 2 - 50);
        this.bttnStart.setLayoutY(this.paneMaze.getHeight() / 2);

        this.ScoreBoard = new Text();


        ScoreBoard.setVisible(false);

        //TODO Streamline this operation
        addToMaze(this.pacManImgView);
        addToMaze(this.blinkyImgView);
        addToMaze(this.pinkyImgView);
        addToMaze(this.bttnStart);
        addToMaze(this.ScoreBoard);


        //TODO Phase out all SVGPath dependencies
        this.pth = new PathTransition();
        this.pth.setNode(this.blinkyImgView);
        this.pth.setPath(this.blinky.svgPath);

        this.pinkyPath = new PathTransition();
        this.pinkyPath.setNode(this.pinkyImgView);
        this.pinkyPath.setPath(this.pinky.svgPath);

        while(this.pinky.SPAWN_PATH == this.blinky.SPAWN_PATH) {
            this.pinky.selectPath();
            this.pinkyPath.setPath(this.pinky.svgPath);
        }

        this.blinky.svgPath.setTranslateX(-440);
        this.blinky.svgPath.setTranslateY(-295);

        this.pinky.svgPath.setTranslateX(-440);
        this.pinky.svgPath.setTranslateY(-295);

        this.pinkyPath.setInterpolator(Interpolator.LINEAR);
        this.pinkyPath.setDuration(Duration.seconds(25));
        this.pinkyPath.setCycleCount(Timeline.INDEFINITE);
        this.pinkyPath.setAutoReverse(true);

        this.pth.setInterpolator(Interpolator.LINEAR);
        this.pth.setDuration(Duration.seconds(25));
        this.pth.setCycleCount(Timeline.INDEFINITE);
        this.pth.setAutoReverse(true);

        this.maze = new Scene(this.paneMaze, 900, 600);

        //TIMELINE AND KEYFRAME
        this.kf = new KeyFrame(Duration.millis(150), e -> {
            checkPacDots();
            this.pacMan.update();
            this.pacMan.posX = this.pacManImgView.getTranslateX();
            this.pacMan.posY = this.pacManImgView.getTranslateY();
        });
        this.tl = new Timeline();
        this.tl.getKeyFrames().add(this.kf);
        this.tl.setCycleCount(Timeline.INDEFINITE);
        this.tl.setRate(2.0);

        this.bttnStart.setOnMouseClicked(e -> { this.bttnStart.setVisible(false); this.tl.play(); this.pinkyPath.play();
            this.pth.play();
            this.pacManImgView.requestFocus();} );

    }

    public void addToMaze(Node object) {
        this.paneMaze.getChildren().add(object);
    }

    //This should load the PacManSprite.png file
    private Image getSpriteImagePath(String path) {

       return new Image(FileLoader.loadImageFile(path).toString());
    }

    @Deprecated
    private Rectangle2D getMazeSprite() {
        return new Rectangle2D(0, 5, 490, 540);
    }

    public StackPane generateMazeLevel() {
        StackPane tmpMazePane = new StackPane();
        Rectangle[] tmpMaze = this.mazeData;

        Circle[] tmpDots = getPacDots();
        this.pacdots = new Circle[tmpDots.length];
        this.pacdots = tmpDots;

        for (int i = 1; i < tmpMaze.length; i++) {
            tmpMazePane.getChildren().add(tmpMaze[i]);
        }

        for (int j = 0; j < tmpDots.length; j++) {
            tmpMazePane.getChildren().add(this.pacdots[j]);
        }


        return tmpMazePane;
    }

    //TODO Review how PacDots are checked and bounds removed, updated & Calculated
    private void checkPacDots() {

        this.pacManBounds = this.pacManImgView.getBoundsInParent();

        for (int i = 0; i < this.pacdots.length; i++) {
            this.pacDotBounds[i] = this.pacdots[i].getBoundsInParent();
        }

        //TODO FIX THIS
        for (int i = 0; i < this.pacDotBounds.length; i++) {
            if (this.pacDotBounds[i].intersects(this.pacManBounds)) {
                this.pacDotBounds[i] = null;
                this.paneMaze.getChildren().remove(this.pacdots[i]);
            }
        }

        int count = 0;
        for (int i = 0; i < this.pacdots.length; i++) {
            if (paneMaze.getChildren().contains(this.pacdots[i])) {

            } else {
                count++;
            }
            if (count == this.pacdots.length) {
                this.tl.pause();
                this.pinkyPath.pause();
                this.pth.pause();
                System.out.println("YOU WIN! √_√");
            }


        }

        if (count == this.pacdots.length) {
            this.tl.pause();
            this.pinkyPath.pause();
            this.pth.pause();
            System.out.println("YOU WIN! √_√");
            ScoreBoard.setText("YOU WIN");
            ScoreBoard.setFill(Color.WHITE);
            ScoreBoard.setFont(Font.font(32));
            ScoreBoard.setVisible(true);
        }
        if (this.pacMan.isDead) {
            ScoreBoard.setText("GHOST WIN");
            ScoreBoard.setFill(Color.WHITE);
            ScoreBoard.setFont(Font.font(32));
            ScoreBoard.setVisible(true);
        }

    }

    private Rectangle[] getMaze() {
        Rectangle[] tmpRect;
        tmpRect = MazeData.generateMazeData();
        return tmpRect;
    }

    private Circle[] getPacDots() {
        Circle[] tmpPacdots = PacDot.generatePacDotData();
        return tmpPacdots;
    }

    public Scene getScene() {
        return this.maze;
    }


}
