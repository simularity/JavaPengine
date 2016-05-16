:- use_module(library(pengines)).
:- use_module(library(http/thread_httpd)).

:- use_module(library(http/http_dispatch)).

server(Port) :-
        http_server(http_dispatch, [port(Port)]).


main :-
    pengine_create([
        server('http://annieslinux:9900/'),
        src_text("
            q(X) :- p(X).
            p(a). p(b). p(c).
	    speak(X) :- pengine_write(X).
        ")
    ]),
    pengine_event_loop(handle, []).


handle(create(ID, _)) :-
    pengine_ask(ID, q(X), [template(X)]).
handle(success(_ID, [X], false)) :-
    writeln(X).
handle(success(ID, [X], true)) :-
    writeln(X),
    pengine_next(ID, []).


:- server(9900).
:- tspy(http_pengine_send).
:- debug(pengine(_)).
:- debug(http(_)).

