package com.devhoss.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.devhoss.app.models.Comentarios;
import com.devhoss.app.models.Usuario;
import com.devhoss.app.models.UsuarioComentarios;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SprintBootReactorApplication  implements CommandLineRunner {


	private static final Logger log = LoggerFactory.getLogger(SprintBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SprintBootReactorApplication.class, args);
		System.out.println("Hola Mundo !!");
	}

	@Override
	public void run(String... args) throws Exception {

		ejemploContraPresion();
	}

	
	// video 23
	
public void ejemploContraPresion() {
		
		Flux.range(1, 10)
		.log()
		//.limitRate(5)
		.subscribe(new Subscriber<Integer>() {
			
			private Subscription s;
			
			private Integer limite = 5;
			private Integer consumido = 0;

			@Override
			public void onSubscribe(Subscription s) {
				this.s = s;
				s.request(limite);
			}

			@Override
			public void onNext(Integer t) {
				log.info(t.toString());
				consumido++;
				if(consumido == limite) {
					consumido = 0;
					s.request(limite);
				}
			}

			@Override
			public void onError(Throwable t) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				
			}
		});
	}

	
	//video 22
	
	public void ejemploIntervalDesdeCreate() {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				private Integer contador = 0;

				@Override
				public void run() {
					emitter.next(++contador);
					if (contador == 10) {
						timer.cancel();
						emitter.complete();
					}

					if (contador == 5) {
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
					}

				}
			}, 1000, 1000);
		}).subscribe(next -> log.info(next.toString()), error -> log.error(error.getMessage()),
				() -> log.info("Hemos terminado"));
	}

	
	//video21
	
	//dar stop
	public void ejemploIntervalInfinito() throws InterruptedException {

		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
		.doOnTerminate(latch::countDown)
		.flatMap(i -> {
			if (i >= 5) {
				return Flux.error(new InterruptedException("Solo hasta 5!"));
			}
			return Flux.just(i);
		})
		.map(i -> "Hola " + i)
		.retry(2)
		.subscribe(s -> log.info(s), e -> log.error(e.getMessage()));

		latch.await();
	}

	
	//video 20
	
	public void ejemploDelayElements() {
		Flux<Integer> rango = Flux.range(1, 12).delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));

		rango.blockLast();
	}

	public void ejemploInterval() {
		Flux<Integer> rango = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

		rango.zipWith(retraso, (ra, re) -> ra)
		.doOnNext(i -> log.info(i.toString()))
		.blockLast();
	}

	//video 19 operador range
	public void ejemploZipWithRangos() {
		Flux<Integer> rangos = Flux.range(0, 4);
		Flux.just(1, 2, 3, 4).map(i -> (i * 2))
				.zipWith(rangos, (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos))
				.subscribe(texto -> log.info(texto));
	}

	//video 18 combinar con zipwith
	public void ejemploUsuarioComentariosZipWithForma2() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));

		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal!");
			comentarios.addComentario("Mañana voy a la playa!");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono.zipWith(comentariosMono).map(tuple -> {
			Usuario u = tuple.getT1();
			Comentarios c = tuple.getT2();
			return new UsuarioComentarios(u, c);
		});

		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	
	public void ejemploUsuarioComentariosZipWith() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));

		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal!");
			comentarios.addComentario("Mañana voy a la playa!");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono
				.zipWith(comentariosMono, (usuario, comentarios) -> new UsuarioComentarios(usuario, comentarios));

		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}
	
	//video 17 combinar 2 flujos con el operador flatmap - conbina usuario con comentarios y se crea usuariocomentario
	public void ejemploUsuarioComentariosFlatMap() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal!");
			comentarios.addComentario("Mañana voy a la playa!");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono
				.flatMap(u -> comentariosUsuarioMono
				.map(c -> new UsuarioComentarios(u, c)));

		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	//video 16 convierte una lista a un mono de list
	public void ejemploCollectListImprimirlistademono() throws Exception {

		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", "Guzman"));
		usuariosList.add(new Usuario("Pedro", "Fulano"));
		usuariosList.add(new Usuario("Maria", "Fulana"));
		usuariosList.add(new Usuario("Diego", "Sultano"));
		usuariosList.add(new Usuario("Juan", "Mengano"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(usuariosList).collectList()
		.subscribe(lista -> {
			lista.forEach(item -> log.info(item.toString()));
		});
	}

	public void ejemploCollectList() throws Exception {

		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", "Guzman"));
		usuariosList.add(new Usuario("Pedro", "Fulano"));
		usuariosList.add(new Usuario("Maria", "Fulana"));
		usuariosList.add(new Usuario("Diego", "Sultano"));
		usuariosList.add(new Usuario("Juan", "Mengano"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(usuariosList)
		.collectList()
		.subscribe(lista -> log.info(lista.toString()));
	}



	// video 15
	public void ejemploUsuarioToString() throws Exception {

		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", "Guzman"));
		usuariosList.add(new Usuario("Pedro", "Fulano"));
		usuariosList.add(new Usuario("Maria", "Fulana"));
		usuariosList.add(new Usuario("Diego", "Sultano"));
		usuariosList.add(new Usuario("Juan", "Mengano"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(usuariosList)
		.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
		.flatMap(nombre -> {
			if (nombre.contains("bruce".toUpperCase())) {
				return Mono.just(nombre);
			} else {
				return Mono.empty();
			}
		}).map(nombre -> {
			return nombre.toLowerCase();
		}).subscribe(u -> log.info(u.toString()));
	}

	//video 14 flatmap la diferencia con map, es que flatmap devuelve un obsevable flux o mono, pero el map te devuelve otra entidad no observable
	public void ejemploFlatMap() throws Exception {

		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Fulano");
		usuariosList.add("Maria Fulana");
		usuariosList.add("Diego Sultano");
		usuariosList.add("Juan Mengano");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");


		Flux.fromIterable(usuariosList)
		.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
		.flatMap(usuario -> {
			if (usuario.getNombre().equalsIgnoreCase("bruce")) {
				return Mono.just(usuario);
			}
			else {
				return Mono.empty();		
			}		

		})
		.map(usuario -> {
			String nombre = usuario.getNombre().toLowerCase();
			usuario.setNombre(nombre);
			return usuario;
		}).subscribe(u -> log.info(u.toString()));
	}



	public void ejemploIterable() throws Exception {

		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Fulano");
		usuariosList.add("Maria Fulana");
		usuariosList.add("Diego Sultano");
		usuariosList.add("Juan Mengano");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		Flux<String> nombres = Flux
				.fromIterable(usuariosList); /*
				 * Flux.just("Andres Guzman" , "Pedro Fulano" , "Maria Fulana",
				 * "Diego Sultano", "Juan Mengano", "Bruce Lee", "Bruce Willis");
				 */

		Flux<Usuario> usuarios = nombres
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce")).doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacíos");
					}

					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				}).map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});

		usuarios.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), new Runnable() {

			@Override
			public void run() {
				log.info("Ha finalizado la ejecución del observable con éxito!");
			}
		});

	}


	//video 13
	public void ejemploiterablelist() {

		//Flux<String> nombres = Flux.just("Andres Guzman","Diego Sultano","Bruce lee","Bruce willss");

		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Fulano");
		usuariosList.add("Maria Fulana");
		usuariosList.add("Diego Sultano");
		usuariosList.add("Juan Mengano");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		Flux<String> nombres = Flux.fromIterable(usuariosList);

		Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario (nombre.split(" ")[0].toUpperCase(),nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().toLowerCase().equals("bruce")) 
				.doOnNext(usuario -> {
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				}).map(u -> {
					String nombre = u.getNombre().toLowerCase();
					u.setNombre(nombre);
					return u;
				});

		usuarios.subscribe(m -> log.info(m.toString()));

	}

	// VIDEO 12 inmutabilidad
	public void ejemplooperadorfilterinmutabilidad1() {

		Flux<String> nombres = Flux.just("Andres Guzman","Diego Sultano","Bruce lee","Bruce willss");

		Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario (nombre.split(" ")[0].toUpperCase(),nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().toLowerCase().equals("bruce")) 
				.doOnNext(usuario -> {
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				}).map(u -> {
					String nombre = u.getNombre().toLowerCase();
					u.setNombre(nombre);
					return u;
				});

		usuarios.subscribe(m -> log.info(m.toString()));

	}

	public void ejemplooperadorfilterinmutabilidad() {

		Flux<String> nombres = Flux.just("Andres Guzman","Diego Sultano","Bruce lee","Bruce willss");

		nombres.map(nombre -> new Usuario (nombre.split(" ")[0].toUpperCase(),nombre.split(" ")[1].toUpperCase()))
		.filter(usuario -> usuario.getNombre().toLowerCase().equals("bruce")) 
		.doOnNext(usuario -> {
			System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
		}).map(u -> {
			String nombre = u.getNombre().toLowerCase();
			u.setNombre(nombre);
			return u;
		});

		nombres.subscribe(m -> log.info(m.toString()));

	}

	// VIDEO 11 OPERADOR FILTER

	public void ejemplooperadorfilter() {

		Flux<Usuario> nombres = Flux.just("Andres Guzman","Diego Sultano","Bruce lee","Bruce willss")
				.map(nombre -> new Usuario (nombre.split(" ")[0].toUpperCase(),nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().toLowerCase().equals("bruce")) 
				.doOnNext(usuario -> {
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				}).map(u -> {
					String nombre = u.getNombre().toLowerCase();
					u.setNombre(nombre);
					return u;
				});

		nombres.subscribe(m -> log.info(m.toString()));

	}

	// video 10 operador map

	public void ejemploOperadorMapCambiandoTipo() {

		Flux<Usuario> nombres = Flux.just("Andres","Pedro","Diego","Juan")
				.map(e -> new Usuario (e.toUpperCase(),null))
				.doOnNext(usuario -> {
					System.out.println(usuario.getNombre());
				}).map(u -> {
					String nombre = u.getNombre().toLowerCase();
					u.setNombre(nombre);
					return u;
				});

		nombres.subscribe(m -> log.info(m.toString()));

	}

	public void ejemploOperadorMapOtroOrden1() {

		Flux<String> nombres = Flux.just("Andres","Pedro","Diego","Juan").map(e -> {
			return e.toUpperCase();
		}).doOnNext(elemento -> {
			System.out.println(elemento);
		}).map(e -> {
			return e.toLowerCase();
		});

		nombres.subscribe(log::info);

	}

	public void ejemploOperadorMapOtroOrden() {

		Flux<String> nombres = Flux.just("Andres","Pedro","Diego","Juan").map(e -> {
			return e.toUpperCase();
		}).doOnNext(elemento -> {
			System.out.println(elemento);
		});

		nombres.subscribe(log::info);

	}

	public void ejemploOperadorMap() {

		Flux<String> nombres = Flux.just("Andres","Pedro","Diego","Juan").doOnNext(elemento -> {
			System.out.println(elemento);
		}).map(e -> {
			return e.toUpperCase();
		});

		nombres.subscribe(log::info);

	}


	//Video 8

	public void ejemploBasicoReferenciadeMetodologemularerror()   throws Exception{

		Flux<String> nombres = Flux.just("Andres","Pedro","","Diego").doOnNext(e-> {
			if(e.isEmpty()) {
				throw new RuntimeException("Nombres no pueden ser vacíos");
			}
			System.out.println(e);
		});
		nombres.subscribe(elemento -> log.info(elemento), error -> log.error(error.getMessage()));
	}

	public void ejemploBasicoReferenciadeMetodolog() {

		Flux<String> nombres = Flux.just("Andres","Pedro","Diego","Jorge").doOnNext(System.out::println);
		nombres.subscribe(log::info);
	}


	///video 7
	public void ejemploBasico() {

		Flux<String> nombres = Flux.just("Andres","Pedro","Diego","Juan").doOnNext(elemento -> {
			System.out.println(elemento);
		} );
		nombres.subscribe();
	}

	public void ejemploBasico1() {

		Flux<String> nombres = Flux.just("Andres","Pedro","Diego").doOnNext(elemento -> System.out.println(elemento));
		nombres.subscribe();
	}

	public void ejemploBasicoReferenciadeMetodo() {

		Flux<String> nombres = Flux.just("Andres","Pedro","Diego","Jorge").doOnNext(System.out::println);
		nombres.subscribe();
	}

}
