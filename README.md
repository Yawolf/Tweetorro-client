<div id="table-of-contents">
<h2>Table of Contents</h2>
<div id="text-table-of-contents">
<ul>
<li><a href="#orgheadline1">1. Introduccion</a></li>
<li><a href="#orgheadline4">2. Instalacion y Ejecucion</a>
<ul>
<li><a href="#orgheadline2">2.1. Instalacion de Scala y SBT</a></li>
<li><a href="#orgheadline3">2.2. Ejecucion del codigo</a></li>
</ul>
</li>
<li><a href="#orgheadline7">3. Implementacion</a>
<ul>
<li><a href="#orgheadline5">3.1. Tweetorro</a></li>
<li><a href="#orgheadline6">3.2. Tweetorro-cli</a></li>
</ul>
</li>
<li><a href="#orgheadline8">4. Dificultades</a></li>
<li><a href="#orgheadline9">5. Conclusiones</a></li>
</ul>
</div>
</div>


# Introduccion<a id="orgheadline1"></a>

Esta es la practica propuesta por la asignatura Middleware en la ETSIINF UPM <sup><a id="fnr.1" class="footref" href="#fn.1">1</a></sup>.

En esta practica se pretende iniciar a lo alumnos en el uso de servicios remotos
y para ello se ha pensado implementar la practica en Java usando el servicio RMI <sup><a id="fnr.2" class="footref" href="#fn.2">2</a></sup>.

Si bien, por parte de los autores de esta practica, Java es un lenguaje obsoleto que 
deberia desaparecer, por tanto la practica esta implementada en Scala <sup><a id="fnr.3" class="footref" href="#fn.3">3</a></sup>, lenguaje
de tendencia funcional, con orientacion a objetos y que ejecuta sobre la JVM <sup><a id="fnr.4" class="footref" href="#fn.4">4</a></sup> 
haciendo, por tanto, posible el uso de todas las librerias compiladas de Java.

# Instalacion y Ejecucion<a id="orgheadline4"></a>

Ante todo es necerario tener Scala instalado en el sistema, asi como SBT <sup><a id="fnr.5" class="footref" href="#fn.5">5</a></sup>.
Tambien, como dependencia del programa, sera necesario tener instalado REDIS <sup><a id="fnr.6" class="footref" href="#fn.6">6</a></sup>, una
base de datos NoSQL del tipo Key -> Value.

## Instalacion de Scala y SBT<a id="orgheadline2"></a>

Para instalar Scala y SBT en debian:

    sudo apt-get install scala sbt

En ArchLinux:

    sudo pacman -S scala sbt

En Windows sera necesario bajarse los ejecutables.

## Ejecucion del codigo<a id="orgheadline3"></a>

Una vez instalado Scala y SBT, hemos configurado el *build.sbt* para que compile y
ejecute automaticamente todo lo necesario, por tanto, dentro de la raiz del proyecto:

    sbt compile

para compilar el codigo.

    sbt run

para ejecutar el codigo. Sera necesario tener el servidor REDIS ejecutando en este punto.

Y listo, ya estaria el programa listo para usarse!

# Implementacion<a id="orgheadline7"></a>

Primero dejar claro que, para evitar conflictos tanto a la hora de desarollar como a la
hora de configurar el *build.sbt*, hemos optado por la solucion mas sencilla, hay 2
proyectos principales:

-   Tweetorro: Contiene todo lo referente al servidor
-   Tweetorro-cli: Contiene todo lo referente al cliente

Ahora pasaremos a describir brevemente cada una de las implementaciones:

## Tweetorro<a id="orgheadline5"></a>

Esta es la implementacion del lado del servidor, es una implementacion sencilla basada
en llamadas a las Base de Datos REDIS y proceso de los resultados de esta. Cabe destacar
que todas las llamadas a REDIS tienen como tipo de retorno *Option[Type]*, los Option <sup><a id="fnr.7" class="footref" href="#fn.7">7</a></sup>
en Scala pretenecen a un tipo de datos que son  una emulacion de las Monadas de Haskell <sup><a id="fnr.8" class="footref" href="#fn.8">8</a></sup>.
Estos tipos de datos ofrecen cosistencia asi como seguridad a nivel de codigo, en 
concreto, *Option[Type]* lo que hace es devolver un resultado de tipo Type o, en su defecto
un *None*, es el equivalente al *Maybe* de Haskell <sup><a id="fnr.9" class="footref" href="#fn.9">9</a></sup>

Se crea un Scala Object <sup><a id="fnr.10" class="footref" href="#fn.10">10</a></sup> en el que se instancia la implementacion de nuestro servidor
y el servicio RMI.

En la implementacion de nuestro servidor se encuentran todas las funciones que se piden:

-   createUser: Crea un nuevo usuario si no existe.
-   login: Login de un usuario al sistema.
-   snedTweet: Envia un tweet.
-   retweet: reenvia un tweet.
-   follow: Seguir a un usuario.
-   unfollow: Dejar de seguir a un usuario.
-   followers: Listado de usuarios a los que sigues.
-   following: Listado de usuarios que te siguen.
-   logoutRemote: logout de un usuario.
-   checkRemoteProfile: Obtencion de datos del perfil de usuario.
-   modifyRemoteProfile: Modificacion de datos del perfin de usuario.
-   getTweets: Timeline de tweets.
-   searchUser: Busqueda de un usuario.
-   sendDM: Envio de mensaje privado.
-   getDM: Obtencion de mensajes privados.

La base de datos esta planteada de la siguiente manera:

![img](./bbdd.png)

## Tweetorro-cli<a id="orgheadline6"></a>

La implementacion de Tweetorro-cli tambien es muy sencilla, y corresponde al lado del 
cliente. Dado que esta parte tambien ha sido escrita en Scala y no conocemos herramientas
de creacion de GUI ni tampoco teniamos tiempo para aprender una nueva, se opto por hacer 
una interfaz de terminal basada en un REPL (Read Evaluate Print Loop) <sup><a id="fnr.11" class="footref" href="#fn.11">11</a></sup> bastante
completo que es, de forma basica, una maquina de estados.

Para hacer los "estados" de REPL se ha optado por in metodo recursivo por cola
(tailrec) <sup><a id="fnr.12" class="footref" href="#fn.12">12</a></sup> que recibe estados en forma de *case objects*.

Desde este REPL se pueden hacer todos los mandatos. Desde logearse hasta cambiar los
datos de perfil de un usuario. Una vez que la aplicacion este ejecutando nos pedira
si queremos loggearno, crear un usuario o salir. Una vez que hayamos hecho el login,
se accede al *MainState* desde el que se puede acceder a cualqueira de los otros estados.
Por lo general, cada funcion implica un cambio de estado, esto es, desde el *MainState* se
puede pedir mandar un tweet, esto llama a la funcion sendTweet que, en caso de terminar
debidamente, devolvera un estado para que sera el siguiente estado de la maquina. En un
ejemplo mas complejo, desde el *MainState* se puede pedir acceder a la seccion *profile*,
esto es, la seccion que nos permite ver y modificar nuestro perfil, y esto manda a la 
maquina de estados un nuevo estado, que es el ProfileState, que nos permitira entrar en
todas las "herramientas" del perfil y volver al *MainState* o en su defecto, permanecer
en el *ProfileState*.

Como se ha comentado, aunque el cliente implementa ciertas funciones de control, como puede
ser el no enviar tweets mas largos de 140 caracteres o imprimir ciertos mensajes de 
error en el caso de que una operacion sea incorrecta, toda la logica del programa se 
ejecuta de cara al servidor, es decir, es en el servidor donde se ejecutan las operaciones
de envio de tweets, y de asignar a cada usuario sus followers y demas. Todo esto se
consigue usando la libreria RMI (Remote Method Invokation) propia de Java. Scala
posee sus propias librerias mas optimas que Java RMI pero, dado que era requisito usar
RMI para realizar la practica y que Scala es compatible con cualquier libreria de Java,
no ha habido mas problema que el usual en la implementaciom.

Otro de los requisitos de la practica es que el cliente ha de poder recibit notificaciones,
para esto se ha hecho un sistema de callback bastante sencillo. El usuario, al hacer login
crea un callback object y lanza un thread que ejecuta su main function, la cual contiene
un "servidor" simple que se queda a la escucha esperando que le notifiquen algo; una vez que
el Thread es lanzado, el cliente le manda al servidor los datos necesarios para poder
conectarse, eso es, el username que se usara como identificador y el puerto en el que esta 
escuchando el cliente.

# Dificultades<a id="orgheadline8"></a>

La verdad es que el proyecto ha ido fluido y sin problemas, en cualquier caso, el mayor
problema que hemos encontrado ha sido la creacion del callback, por lo demas todo ha ido
bastante normal.

# Conclusiones<a id="orgheadline9"></a>

Lo primero decir que el proyecto propuesto ha sido, cuanto menos, divertido ya que se 
implementa un clone de algo que es usado por muchos usuarios todos los dias, y hacer
cosas que funcionan y se usan mola.

La filosofia de que el cliente hace cosas sencillas de control, pero que la logica del
programa la lleva el servidor, es algo que vivimos en el dia a dia con las aplicaciones
moviles pero es muy interesante saber como implementar dicha logica remota y conseguir
un funcionamiento muy bueno.

Como bonus, nosotros nos lo hemos pasado rematadamente bien porque, al elegir Scala como
lenguaje en vez de java, hemos tenido que aprender muchas cosas, y aprender cosas es 
muy divertido. Es interesante ver que existen lenguajes muy buenos y muy funcionales
que implementan conceptos de otrso lenguajes maravillosos pero no tan sencillos, este es
el ejemplo de la simulacion de Monadas en Scala que son en si una copia de las Monad de 
Haskell.

En conclusion, que ha sido todo muy divertido :D

<div id="footnotes">
<h2 class="footnotes">Footnotes: </h2>
<div id="text-footnotes">

<div class="footdef"><sup><a id="fn.1" class="footnum" href="#fnr.1">1</a></sup> <div class="footpara">Universidad Politecnica de Madird: <https://www.fi.upm.es/></div></div>

<div class="footdef"><sup><a id="fn.2" class="footnum" href="#fnr.2">2</a></sup> <div class="footpara">RMI: <http://docs.oracle.com/javase/7/docs/api/java/rmi/package-summary.html></div></div>

<div class="footdef"><sup><a id="fn.3" class="footnum" href="#fnr.3">3</a></sup> <div class="footpara">Scala: <http://www.scala-lang.org/></div></div>

<div class="footdef"><sup><a id="fn.4" class="footnum" href="#fnr.4">4</a></sup> <div class="footpara">JVM: <https://en.wikipedia.org/wiki/Java_virtual_machine></div></div>

<div class="footdef"><sup><a id="fn.5" class="footnum" href="#fnr.5">5</a></sup> <div class="footpara">SBT: <http://www.scala-sbt.org/></div></div>

<div class="footdef"><sup><a id="fn.6" class="footnum" href="#fnr.6">6</a></sup> <div class="footpara">REDIS: <http://redis.io/></div></div>

<div class="footdef"><sup><a id="fn.7" class="footnum" href="#fnr.7">7</a></sup> <div class="footpara">*Option*: <http://www.scala-lang.org/api/current/index.html#scala.Option></div></div>

<div class="footdef"><sup><a id="fn.8" class="footnum" href="#fnr.8">8</a></sup> <div class="footpara">Monad Haskell: <https://wiki.haskell.org/Monad></div></div>

<div class="footdef"><sup><a id="fn.9" class="footnum" href="#fnr.9">9</a></sup> <div class="footpara">Maybe Haskell: <https://hackage.haskell.org/package/base-4.8.1.0/docs/Data-Maybe.html></div></div>

<div class="footdef"><sup><a id="fn.10" class="footnum" href="#fnr.10">10</a></sup> <div class="footpara">Scala Object: <http://docs.scala-lang.org/tutorials/tour/singleton-objects.html></div></div>

<div class="footdef"><sup><a id="fn.11" class="footnum" href="#fnr.11">11</a></sup> <div class="footpara">REPL: <https://en.wikipedia.org/wiki/Read%E2%80%93eval%E2%80%93print_loop></div></div>

<div class="footdef"><sup><a id="fn.12" class="footnum" href="#fnr.12">12</a></sup> <div class="footpara">tailrec: <https://en.wikipedia.org/wiki/Tail_call></div></div>


</div>
</div>
