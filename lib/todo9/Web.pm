package todo9::Web;

use strict;
use warnings;
use utf8;
use Kossy;
use Digest::SHA;
#use DBI;
#use DBD::mysql;
use DBIx::Sunny;
use Data::Dumper;

sub dbh {
    my $self = shift;
    $self->{_dbh} ||= DBIx::Sunny->connect("dbi:mysql:dbname=todo9",'denaexe','denadena',{
        Callbacks => {
            connected => sub {
                my $conn = shift;
                $conn->do(<<EOF);
CREATE TABLE IF NOT EXISTS entry (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    nickname VARCHAR(255) NOT NULL,
    body TEXT,
    created_at DATETIME NOT NULL
);
EOF
                $conn->do(q{CREATE INDEX IF NOT EXISTS index_created_at ON entry ( created_at )});
                return;
            },
        },
    });
}

filter 'set_title' => sub {
    my $app = shift;
    sub {
        my ( $self, $c )  = @_;
        $c->stash->{site_name} = __PACKAGE__;
        $app->($self,$c);
    }
};

get '/' => sub {
    my ( $self, $c )  = @_;
    my $result = $c->req->validator([
        'offset' => {
            default => 0,
            rule => [
                ['UINT','ivalid offset value'],
            ],
        },
    ]);
    $c->halt(403) if $result->has_error;
    my ($entries,$has_next) = $self->entry_list($result->valid('offset'));
    $c->render('index.tx', {
        offset => $result->valid('offset'),
        entries => $entries,
        has_next => $has_next,
    });
};

get '/edit' => sub {
    my ( $self, $c )  = @_;

    $c->render('edit.tx', {

    });
};

post '/edit' => sub {
    my ( $self, $c )  = @_;
    my $result = $c->req->validator([
	'id' => {
	    rule => [
		['NOT_NULL', 'empty id'],
    	    ],
    	},
    	'body' => {
	    rule => [
		['NOT_NULL', 'empty body'],
	    ],
    	}
    ]);
    $c->halt(403) if $result->has_error;

    my $id = $result->valid('id');
    my $body = $result->valid('body');

    $c->render('edit.tx', {
        # offset => $result->valid('offset'),
        id => $id,
        body => $body,
    });
};

post '/create' => sub {
    my ( $self, $c )  = @_;
    my $result = $c->req->validator([
        'body' => {
            rule => [
                ['NOT_NULL','empty body'],
            ],
        },
        'nickname' => {
            default => 'anonymous',
            rule => [
                ['NOT_NULL','empty nickname'],
            ],
        }
    ]);
    if ( $result->has_error ) {
        return $c->render_json({ error => 1, messages => $result->errors });
    }
    my $id = $self->add_entry(map {$result->valid($_)} qw/body nickname/);
    $c->render_json({ error => 0, location => $c->req->uri_for("/")->as_string });
};

post '/update' => sub {
    my ( $self, $c )  = @_;
    my $result = $c->req->validator([
	'id' => {
	    rule => [
		['NOT_NULL', 'empty id'],
    	    ],
    	},
    	'body' => {
	    rule => [
		['NOT_NULL', 'empty body'],
	    ],
    	}
    ]);
    $c->halt(403) if $result->has_error;

    if ( $result->has_error ) {
        return $c->render_json({ error => 1, messages => $result->errors });
    }
    my $id = $self->edit_entry(map {$result->valid($_)} qw/id body/);
    $c->redirect('/');
};

post '/delete' => sub {
    my ( $self, $c )  = @_;
    my $result = $c->req->validator([
	'id' => {
	    rule => [
		['NOT_NULL', 'empty id'],
    	    ],
    	}
    ]);
    $c->halt(403) if $result->has_error;

    if ( $result->has_error ) {
        return $c->render_json({ error => 1, messages => $result->errors });
    }
    my $id = $self->delete_entry(map {$result->valid($_)} qw/id/);
    $c->redirect('/');
};

get '/json' => sub {
    my ( $self, $c )  = @_;
    my $result = $c->req->validator([
        'q' => {
            default => 'Hello',
            rule => [
                [['CHOICE',qw/Hello Bye/],'Hello or Bye']
            ],
        }
    ]);
    $c->render_json({ greeting => $result->valid->get('q') });
};

sub add_entry {
    my $self = shift;
    my (  $body, $nickname ) = @_;
    $body = '' if ! defined $body;
    $nickname = 'anonymous' if ! defined $nickname;
    my $id = substr Digest::SHA::sha1_hex($$ . join("\0", @_) . rand(1000) ), 0, 16;
    $self->dbh->query(
        q{INSERT INTO entry (id,nickname,body,created_at) VALUES ( ?, ?, ?, now() )},
        $id, $nickname, $body
    );
    return $id;
}

sub delete_entry {
    my $self = shift;
   my ($id) = @_;
    #$id = '' if ! defined $id;
    #die $id;
    $self->dbh->query(
        q{DELETE FROM entry WHERE id = ? },
        $id
    );
    return $id;
}

sub edit_entry {
    my $self = shift;
    my (  $id, $body ) = @_;
    $body = '' if ! defined $body;
    $self->dbh->query(
        q{UPDATE entry SET body = ?, created_at = now() WHERE id = ? },
        $body, $id
    );
    return $id;
}

sub entry_list {
    my $self = shift;
    my $offset = shift;
    $offset = 0 if defined $offset;
    my $rows = $self->dbh->select_all(
        q{SELECT * FROM entry ORDER BY created_at DESC LIMIT ?,11},
        $offset
    );
    my $next;
    $next = pop @$rows if @$rows > 10;
    return $rows, $next;
}


1;
