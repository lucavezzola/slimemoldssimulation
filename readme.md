Di seguito trovi una spiegazione dettagliata e corretta del progetto:

---

# Simulazione di Muffa Policefala

La simulazione riproduce il comportamento emergente di *Physarum polycephalum* (muffa policefala) attraverso un elevato numero di agenti, detti **sonde** (*probes*), che interagiscono in un ambiente in cui depositano e percepiscono tracce. Le interazioni locali tra sonde e tracce generano pattern globali complessi e dinamici.

---

## 1. Parametri Globali

Il file `Constants.java` definisce i parametri base della simulazione:

- **WORLD_WIDTH**: Larghezza del mondo in pixel.
- **WORLD_HEIGHT**: Altezza del mondo in pixel, calcolata con un rapporto 16:9.
- **PROBES_NUMBER**: Numero totale di sonde (in questo caso, 100000).

```java
public class Constants {
    public static final int WORLD_WIDTH = 1500; // In pixels
    public static final int WORLD_HEIGHT = (int)(WORLD_WIDTH * 9 / 16); // In pixels
    public static final int PROBES_NUMBER = 100000;
}
```

---

## 2. La Classe `Main`

La classe `Main` è il punto di ingresso della simulazione e gestisce il ciclo principale (render loop) tramite l'estensione di `ApplicationAdapter`.

### Funzionalità principali

- **Setup della Fotocamera**:  
  Utilizza un'`OrthographicCamera` per visualizzare l'intera area della simulazione.

- **Gestione del Mondo**:  
  Crea un’istanza di `WorldGrid`, che si occupa di gestire l'ambiente, le sonde e le tracce.

- **Ciclo di Aggiornamento**:
  - **Temporizzazione**:  
    Utilizza `TimeUtils.nanoTime` per eseguire operazioni periodiche, come il cambio di colore delle tracce ogni 15 secondi.
    
  - **Pausa/Ripresa**:  
    La simulazione può essere messa in pausa premendo la barra spaziatrice.
    
  - **Aggiornamento della Simulazione**:  
    Se non in pausa, vengono aggiornate le posizioni delle sonde e la "trail map" mediante `worldGrid.updateLogic()` e successivamente viene applicato un effetto di sfocatura usando `worldGrid.blurTrails()`.
    
  - **Rendering**:  
    Infine, la griglia (modificata da `WorldGrid`) viene disegnata su schermo utilizzando uno `SpriteBatch`.

```java
public class Main extends ApplicationAdapter {
    // ... definizione di batch, camera, worldGrid, e variabili di temporizzazione

    @Override
    public void create() {
        // Impostazione della camera
        cam = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();
        
        // Creazione del mondo e inizializzazione della simulazione
        worldGrid = new WorldGrid();
        batch = new SpriteBatch();
        
        startTimeNs = TimeUtils.nanoTime(); // tempo iniziale
        simulationTimeNs = 1000000000; // 1 secondo in nanosecondi
    }

    @Override
    public void render() {
        // Cambio di colore delle tracce ogni 15 secondi
        if (TimeUtils.timeSinceNanos(startTimeNs) > simulationTimeNs * 15) {
            worldGrid.randomizeTrailsColor();
            System.out.println("Changing color....");
            startTimeNs = TimeUtils.nanoTime();
        }

        // Toggle pausa con la barra spaziatrice
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            isPaused = !isPaused;
        }

        if (!isPaused) {
            worldGrid.updateLogic();
            worldGrid.blurTrails();
        }

        worldGrid.manageInputs();

        batch.setProjectionMatrix(cam.combined);
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        batch.begin();
        worldGrid.draw(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        worldGrid.dispose();
    }
}
```

---

## 3. La Classe `WorldGrid`

Questa classe gestisce l'**ambiente** in cui le sonde operano. Le sue componenti principali sono:

### 3.1. Gestione delle Sonde

- **Inizializzazione**:  
  Le sonde vengono istanziate e posizionate casualmente all'interno di un cerchio (centrato nello schermo) con direzione iniziale orientata verso il centro.
  
  ```java
  for (int i = 0; i < probes.length; i++) {
      // Calcola angolo e distanza per posizionare la sonda in un cerchio
      // Imposta la direzione verso il centro
  }
  ```

### 3.2. La "Trail Map"

- **Struttura**:  
  Una matrice bidimensionale di `float` (`trailMap[WORLD_WIDTH][WORLD_HEIGHT]`) che conserva l'intensità delle tracce in ogni pixel.
  
- **Decadimento**:  
  Ad ogni aggiornamento, i valori della traccia si riducono (moltiplicandoli per `vanishing_factor`), simulando la dissipazione naturale.

```java
public void updateLogic() {        
    for (int i = 0; i < trailMap.length; i++) {
        for (int j = 0; j < trailMap[i].length; j++) {
            trailMap[i][j] *= vanishing_factor;
        }
    }
    
    // Aggiorna le posizioni delle sonde, che depositeranno nuove tracce
    for (Probe probe : probes) {
        probe.updatePosition(trailMap);
    }
}
```

### 3.3. Effetto di Blurring

- **Scopo**:  
  Diffondere le tracce applicando una media locale (su una finestra 3×3), ottenendo e interpolando la media con il valore originale usando il coefficiente `alpha`.

```java
public void blurTrails() {
    float[][] tempTrailMap = new float[trailMap.length][trailMap[0].length];

    for (int i = 0; i < trailMap.length; i++) {
        for (int j = 0; j < trailMap[i].length; j++) {
            float sum = 0;
            int count = 0;

            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    int posX = i + offsetX;
                    int posY = j + offsetY;

                    if (posX >= 0 && posX < WORLD_WIDTH && posY >= 0 && posY < WORLD_HEIGHT) {
                        sum += trailMap[posX][posY];
                        count++;
                    }
                }
            }

            float blurredValue = sum / count;
            tempTrailMap[i][j] = alpha * blurredValue + (1 - alpha) * trailMap[i][j];
        }
    }

    // Aggiorna la trailMap con i valori sfumati
    for (int i = 0; i < trailMap.length; i++) {
        for (int j = 0; j < trailMap[i].length; j++) {
            trailMap[i][j] = tempTrailMap[i][j];
        }
    }
}
```

### 3.4. Rendering

- **Disegno delle Tracce e delle Sonde**:  
  Utilizza un `Pixmap` per colorare ogni pixel in base all'intensità nella `trailMap` e disegna le sonde come pixel (depositando nuove tracce locali).
  
- **Aggiornamento della Texture**:  
  Dopo aver aggiornato il `Pixmap`, la texture associata viene disegnata sullo schermo.

- **Aggiornamento del Colore**:  
  I valori RGB (utilizzati per colorare le tracce) vengono miscelati lentamente con nuovi valori casuali tramite `randomizeTrailsColor()`, garantendo un effetto visivo dinamico.

### 3.5. Gestione degli Input

Il metodo `manageInputs()` permette di modificare diversi parametri della simulazione in tempo reale:
- **Velocità della sonda** (`velocity`)
- **Angolo dei sensori** (`sensor_angle_space`)
- **Velocità di rotazione** (`turning_speed`)
- **Distanza di offset e raggio del sensore**
- **Decadimento delle tracce** (`vanishing_factor`) e intensità del blur (`alpha`)
- Attivazione/disattivazione del **looping dei bordi** (quando le sonde escono, riappaiono dall'altra parte)
- Visualizzazione dei parametri correnti

---

## 4. La Classe `Probe`

La classe `Probe` definisce il comportamento individuale di ciascun agente.

### 4.1. Attributi

- **Posizione e Direzione**:  
  Ogni sonda ha:
  - Una posizione (`Vector2`)
  - Una direzione (espressa in radianti)

- **Parametri Statici Condivisi**:  
  Questi parametri influenzano il comportamento di tutte le sonde e includono:
  - `velocity` – velocità di movimento
  - `sensor_angle_space` – ampiezza dell'area di sensing laterale
  - `turning_speed` – velocità con cui la sonda può cambiare direzione
  - `sensor_offset_distance` – distanza dal centro della sonda in cui avviene il sensing
  - `sensor_radius` – dimensione dell'area che il sensore analizza
  - `loopingBorders` – abilita/disabilita il wrapping dei bordi

### 4.2. Aggiornamento della Posizione

Nel metodo `updatePosition()`:
- **Aggiornamento della Direzione**:  
  Prima di muoversi, la sonda chiama `updateDirection()` per modificare la propria direzione in base ai segnali rilevati.
  
- **Movimento**:  
  La posizione viene aggiornata aggiungendo un vettore derivato dalla direzione corrente e dalla velocità:
  - Se **loopingBorders** è attivo, la sonda "riappare" dall'altro lato del mondo se supera i confini.
  - Altrimenti, la sonda viene costretta a rimanere all'interno dei bordi; se supera i limiti, la sua direzione viene randomizzata.

### 4.3. Aggiornamento della Direzione

Il metodo `updateDirection()` sfrutta il concetto di **sensing**:
- **Sensing**:  
  La sonda esegue tre rilevamenti:
  - **Fronte** (senza offset)
  - **Sinistra** (con offset angolare positivo)
  - **Destra** (con offset angolare negativo)
  
  Il metodo `sense()` calcola, per ciascun orientamento, il valore medio delle tracce in un’area definita da `sensor_radius`, a una distanza `sensor_offset_distance` dalla posizione della sonda.
  
- **Decisione e Rotazione**:  
  Vengono confrontati i valori medi rilevati:
  - Se il valore frontale supera entrambi quelli laterali, la sonda prosegue dritta.
  - Se entrambi i sensori laterali rilevano maggiori intensità rispetto al fronte, la sonda effettua una rotazione casuale (compresa tra sinistra e destra).
  - Se uno solo dei lati ha un valore superiore, la sonda ruota verso quella direzione in proporzione alla velocità di rotazione e al delta del tempo.

```java
private void updateDirection(float[][] trailMap) {
    float deltaTime = Gdx.graphics.getDeltaTime();
    float weightFront = sense(0, trailMap);
    float weightLeft = sense(sensor_angle_space, trailMap);
    float weightRight = sense(-sensor_angle_space, trailMap);
    
    if (weightFront > weightLeft && weightFront > weightRight) {
        // Mantiene la direzione
    } else if (weightLeft > weightFront && weightRight > weightFront) {
        // Rotazione casuale tra sinistra e destra
        direction += (randomFloatFrom0To1() - 0.5f) * 2 * turning_speed * deltaTime;
    } else if (weightLeft > weightRight) {
        // Ruota a sinistra
        direction += randomFloatFrom0To1() * turning_speed * deltaTime;
    } else if (weightRight > weightLeft) {
        // Ruota a destra
        direction -= randomFloatFrom0To1() * turning_speed * deltaTime;
    }
}
```

- **Il Metodo `sense()`**:  
  Calcola la media dei valori nella porzione di `trailMap` che il sensore analizza. Se il flag `loopingBorders` è attivo, il sensing tiene conto del wrapping dei bordi.

---

## Conclusioni

La simulazione sfrutta un approccio **biomimetico** (che tenta di emulare i processi degli organismi viventi) in cui:

- Le **sonde** si muovono depositando tracce e reagendo alla densità locale di queste ultime.
- La **trail map** evolve nel tempo grazie al decadimento naturale e all’effetto di sfocatura, diffondendo le tracce.
- L’interazione tra questi elementi genera comportamenti emergenti e pattern dinamici, aspetto che ricorda il funzionamento reale della muffa policefala.

Inoltre, la possibilità di variare in tempo reale diversi parametri (come velocità, angoli dei sensori, decadimento, e altri) rende questa simulazione uno strumento interessante sia dal punto di vista visivo che concettuale, con potenziali applicazioni in aree quali il design generativo e l’ottimizzazione distribuita.

---