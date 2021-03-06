package coinffeine.gui.application.splash

import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.control.Label
import scalafx.scene.layout._
import scalafx.stage.StageStyle

import coinffeine.gui.application.updates.CoinffeineVersion
import coinffeine.gui.control.Spinner
import coinffeine.gui.scene.CoinffeineScene
import coinffeine.gui.scene.styles.Stylesheets

object SplashScreen {

  def displayOn(stage: PrimaryStage): Unit = {
    stage.initStyle(StageStyle.UNDECORATED)
    stage.scene = new CoinffeineScene(Stylesheets.Splash) {
      root = new VBox {
        id = "splash-root"
        content = Seq(
          new Spinner,
          new HBox {
            content = Seq(
              new Label("Copyright (C) 2014-2015 Coinffeine S.L.") {
                id = "copyright-note"
              },
              new Label(s"Version ${CoinffeineVersion.Current}") {
                id = "version-note"
              })
          })
      }
    }
    stage.show()
  }
}
