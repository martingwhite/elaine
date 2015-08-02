function [TRANS_HAT,EMIS_HAT] = train(num_states,num_emissions)

% each trace is stored in a [1-9][0-9]*.dat
data_path = '../Hmm/*.dat'; % FIXME

% code for generating dummy data...
%{
TRANS = [0.90 0.10; 0.05 0.95];
EMIS = [1/6 1/6 1/6 1/6 1/6 1/6; 7/12 1/12 1/12 1/12 1/12 1/12];

for i = 1:2
    [seq,states] = hmmgenerate(100/i,TRANS,EMIS);
    disp('size of seq...');
    disp(num2str(size(seq)));
    disp('size of states...');
    disp(num2str(size(states)));
    data = [seq;states];
    disp('size of data...');
    disp(num2str(size(data)));
    fid = fopen(strcat(num2str(i),'.dat'),'w');
    fprintf(fid,'%d\t%d\n',data);
    fclose(fid);
end
%}
% glob the files containing the traces...
files = dir(data_path);

disp('number of files...');
disp(num2str(length(files)));

corpus_seq = cell(length(files),1);
corpus_states = cell(length(files),1);

i = 1; % corpora are 1-indexed
for file = files'
    disp('filename...');
    disp(file.name);
    trace = load(file.name); % assumes two columns of tab delimited ints
    trace = trace'; % 2-by-m since hmm utils operate on rows of data
    disp('size of trace...');
    disp(num2str(size(trace))); % 2-by-m where m is $ wc -l file
    corpus_seq{i} = trace(1,:);
    corpus_states{i} = trace(2,:);
    i = i + 1;
end

disp('size of emissions corpus...');
disp(num2str(size(corpus_seq)));

disp('size of states corpus...');
disp(num2str(size(corpus_states)));

% either construct random TRANS and EMIS matrices...
TRGUESS = rand(num_states); % rand \sim U[0,1]
row_sum = sum(TRGUESS,2);
TRGUESS = bsxfun(@rdivide,TRGUESS,row_sum); % TRGUESS is row stochastic
disp('TRGUESS...');
disp(TRGUESS);

EMITGUESS = rand(num_states,num_emissions);
row_sum = sum(EMITGUESS,2);
EMITGUESS = bsxfun(@rdivide,EMITGUESS,row_sum);
disp('EMITGUESS...');
disp(EMITGUESS);

% ...or load ML estimates HERE...
%TRGUESS = load('TRGUESS.dat');
%EMITGUESS = load('EMITGUESS.dat');

% apply pseudotransitions and pseudoemissions accordingly...
[ESTTR,ESTEMIT] = hmmtrain(corpus_seq,TRGUESS,EMITGUESS,...
    'Maxiterations',1000,'Tolerance',1e-6);
disp('ESTTR...');
disp(ESTTR);
disp('ESTEMIT...');
disp(ESTEMIT);

% changing the initial state distribution...
p = repmat(1/num_states,1,num_states); % p \sim U[1,num_states]
disp('p...');
disp(p);
TRANS_HAT = [0 p; zeros(size(ESTTR,1),1) ESTTR];
disp('TRANS_HAT...');
disp(TRANS_HAT);
EMIS_HAT = [zeros(1,size(ESTEMIT,2)); ESTEMIT];
disp('EMIS_HAT...');
disp(EMIS_HAT);
